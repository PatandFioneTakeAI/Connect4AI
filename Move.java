
public class Move {
	EToken[][] board;
	int score; 
	int col; 
	int type; 
	
	public Move(EToken[][] board){
		this.board = board;
	}
	
	public Move(EToken[][] board, int col, int type){
		this.board = board; 
		this.col = col;
		this.type = type; 
	}
	
	public String getInstructions(){
		return this.col + " " + this.type;
	
	}
	
	public EToken[][] getBoard(){
		return this.board;
	}
	
	public int getScore(){
		return this.score;
	}
	
	public int getCol(){
		return this.col;
	}
	
	
	public void setScore(int value){
		this.score = value; 
	}
	
	public boolean isPop(){
		if(this.type == 0){
			return true;
		}
		else{
			return false; 
		}
	}

}




