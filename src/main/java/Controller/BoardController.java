package Controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;

import DAO.BoardDao;
import DTO.Board;

@WebServlet("/")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BoardDao dao;
	private ServletContext ctx;

	@Override
	public void init() throws ServletException {
		super.init();

		// init은 서블릿 객체 생성시 딱 한번만 실행하므로 객체를 한번만 생성해 공유 할 수 있다.
		dao = new BoardDao();
		ctx = getServletContext(); // 웹 어플리케이션의 자원 관리

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); // request 한글 깨짐 방지
		dopro(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); // 한글 깨짐 방지
		dopro(request, response);
	}

	protected void dopro(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 라우팅의 역활 : 페이지의 url 경로를 찾아줌

		String context = request.getContextPath();
		String command = request.getServletPath();
		String site = null;
		System.out.println(command);
		System.out.println(context + " 2");
		// 경로 라우팅(경로를 찾아줌)
		switch (command) {
		case "/list":
			site = getList(request);
			break;
		case "/view":
			site = getView(request);
			break;
		case "/write": //글쓰기 화면을 보여줌
			site = "write.jsp";
			break;
			
		case "/insert": //insert기능
			site = insertBoard(request);
			break;
		}
		// 둘다 페이지를 이동한다
		// redirect: URL의 변화 O, 객체의 재사용 X (request, response객체)
		// *DB에 변화가 생기는 요청에 사용(글쓰기, 회원가입....) insert, update , delete 조심
		
		// forward: URL의 변화 X(보안..), 객체의 재사용 O
		// *단순 조회(리스트보기, 검색)
		
		if(site.startsWith("redirect:/")) { //redirect
			String  rview = site.substring("redirect:/".length());
			System.out.println(rview);
			response.sendRedirect(rview);
		}else {//forward
			ctx.getRequestDispatcher("/" +  site).forward(request, response);
		}
	}

	
	
		public String getList(HttpServletRequest request) {
			List<Board> list;
			
			try {
				list = dao.getList(); // getList에서 던저진 에러를 이메서드에서 받음
				request.setAttribute("boarList", list);
			} catch (Exception e) {
				e.printStackTrace();
				ctx.log("게시판 목록 생성 과정에서 문제 발생");
				//사용자 한테 에러메세지를 보여주기 위해 저장
				request.setAttribute("error", "게시판 목록이 정상적으로 처리되지 않았습니다!");
			} 
			
			return "index.jsp";
		}
		
		public String getView(HttpServletRequest request) {
			int board_no = Integer.parseInt(request.getParameter("board_no"));
			try {
				dao.updateViews(board_no); //조회수 증가
				Board b = dao.getview(board_no);
				request.setAttribute("board", b);
			} catch (Exception e) {
				e.printStackTrace();
				ctx.log("추가 과정에서 문제 발생");
				//사용자 한테 에러메세지를 보여주기 위해 저장
				request.setAttribute("error", "게시글을 정상적으로 가져오지 못했습니다.!");
			} 
			
			return "view.jsp";
		}
		
		public String insertBoard(HttpServletRequest request) {
//			request.getParameter("user_id");
//			request.getParameter("title");
//			request.getParameter("content");
			Board b = new Board();
			
			try {
				BeanUtils.populate(b, request.getParameterMap());
				dao.insertBoard(b);
			} catch (Exception e) {
				e.printStackTrace();
				ctx.log("추가 과정에서 문제 발생");
				//사용자 한테 에러메세지를 보여주기 위해 저장
				request.setAttribute("error", "게시글을 정상적으로 등록되지 않았습니다!");
				return getList(request);
			}
			
			return "redirect:/list";
		}
}
