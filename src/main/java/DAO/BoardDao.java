package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import DTO.Board;

public class BoardDao {
	final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
	final String JDBC_URL = "jdbc:oracle:thin:@localhost:1521:xe";


	// 리소스 방식
	// 데이터 베이스와 연결 수행 메소드
	public Connection open() {
		Connection conn = null;
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(JDBC_URL, "test", "test1234");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn; // 데이터 베이스의 연결 객체를 리턴해줌
	}

	// 게시판 리스트 가져오기
	public ArrayList<Board> getList() throws Exception {

		Connection conn = open();
		ArrayList<Board> boardList = new ArrayList<>(); // board 객체를 저장할 arraylist
		String sql = " select board_no, title, user_id, to_char(reg_date,'yyyy.mm.dd')reg_date, views from board ";
		
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		
		//리소스 자동닫기(try-with-resource)
		try(conn; ps; rs ){//자동으로 안에 있는 리소스를 닫아줌
			while(rs.next()) {
				Board board = new Board();
				board.setBoard_no(rs.getInt(1));
				board.setTitle(rs.getString(2));
				board.setUser_id(rs.getString(3));
				board.setReg_date(rs.getString(4));
				board.setViews(rs.getInt(5));
//				board.setContent(rs.getString(6));
				
				boardList.add(board);
			}
		}
		
		return boardList;
	}
	
	//게시판 내용 가져오기
	public Board getview(int board_no) throws Exception {
		Connection conn = open();
		Board b = new Board();
		
		String sql = " select board_no, title, user_id, to_char(reg_date,'yyyy.mm.dd')reg_date, views,content from board where board_no = ? ";	
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, board_no); // 첫번째 물을표에 매개변수를 집어 넣겠다
		ResultSet rs = ps.executeQuery(); // 쿼리문 실행 -> 데이터 베이스 결과 저장
		
		try(conn; ps; rs){
			while(rs.next()) {
				b.setBoard_no(rs.getInt(1));
				b.setTitle(rs.getString(2));
				b.setUser_id(rs.getString(3));
				b.setReg_date(rs.getString(4));
				b.setViews(rs.getInt(5));
				b.setContent(rs.getString(6));
			}
		}
		return b;
	}
	
	//조회수 올리기
	public void updateViews(int board_no) throws Exception {
		Connection conn = open();
		
		String sql = "update board set views = (views + 1) where board_no = ? ";
		PreparedStatement ps = conn.prepareStatement(sql);
		
		try(conn; ps){
			ps.setInt(1, board_no);
			ps.executeUpdate();
		}

	}

	public void insertBoard(Board b) throws Exception {
		Connection conn = open();
		String sql = "insert into board (board_no, user_id, title, content, reg_date, views) values(board_seq.nextval, ?, ?, ?, sysdate, 0)";
		
		 PreparedStatement ps = conn.prepareStatement(sql);
		 
		 try(conn; ps){
			 ps.setString(1, b.getUser_id());
			 ps.setString(2, b.getTitle());
			 ps.setString(3, b.getContent());
			 ps.executeUpdate();
		 }
	}
}
