import java.util.ArrayList;


public class ConnectNAI {
	
	private static final int DEPTH = 3;
	
	//Game tracker
	private boolean meHasPopped;
	private boolean opponentHasPopped;
	
	//AI tracker
	private boolean ai_meHasPopped;
	private boolean ai_opponentHasPopped;
	private EToken[][] candidateBoard;
	
	public ConnectNAI(){
		this.meHasPopped = false;
		this.opponentHasPopped = false;
		
		this.manageRefereeInteractions();
	}
	
	// Handles all interaction between the referee and the AI
	private void manageRefereeInteractions(){
		//TODO [PAT]
		
		//.....Somewhere goes:
			this.miniMax(null,0,0,0);
			candidateBoard.notify();
	}
	
	//Initializes mini-max process
	private int miniMax(EToken[][] board, int alpha, int beta, int depth){
		return this.max(board, alpha, beta, -ConnectNAI.DEPTH, 0);
	}
	
	//Executes logic for 'max player' turn
	private int max(EToken[][] board, int alpha, int beta, int depthGoal, int currentDepth){
		if(currentDepth==depthGoal)
			return eval(board);
		
		//Compile list of moves
		ArrayList<EToken[][]> children = new ArrayList<EToken[][]>();
		for(int i=0; i<board[0].length; i++){
			if(canPlace(board,i,true))
				children.add(this.place(board,i,true));
			if(canPop(board,i,true))
				children.add(this.pop(board,i,true));
		}
		
		//Determine best move
		int currentScore = Integer.MIN_VALUE;
		EToken[][] bestCandidate = new EToken[board.length][board[0].length];
		for(EToken[][] candidateBoard:children){
			int score = min(candidateBoard,alpha,beta,depthGoal,currentDepth+1);
			if(score > currentScore){
				bestCandidate = candidateBoard;
				currentScore = score;
			}
			alpha = Math.max(currentScore, alpha);
			
			//Prune
			if(alpha > beta)
				break;
		}
		if(currentDepth==0)
			this.candidateBoard = bestCandidate;
		return currentScore;
	}
	
	
	private int min(EToken[][] board, int alpha, int beta, int depthGoal, int currentDepth){
		if(depthGoal==currentDepth)
			return eval(board);
		
		//Build array of potential moves
		ArrayList<EToken[][]> children = new ArrayList<EToken[][]>();
		for(int i=0; i<board[0].length; i++){
			if(canPlace(board,i,true))
				children.add(this.place(board,i,true));
			if(canPop(board,i,true))
				children.add(this.pop(board,i,true));
		}
		
		//Evaluate moves
		int currentScore = Integer.MAX_VALUE;
		for(EToken[][] candidateBoard:children){
			currentScore = Math.min(currentScore, max(candidateBoard,alpha,beta,depthGoal,currentDepth+1));
			beta = Math.min(currentScore, beta);
			
			//Prune
			if(alpha > beta)
				return currentScore;
		}
		return currentScore;
	}
	
	//Determines if a 'place' move is legal or not
	private boolean canPlace(EToken[][] board, int column, boolean isMax){
		if(column < 0 || column > board[0].length)
			return false;
		for(int i = board.length-1; i>=0; i--){
			EToken token = board[i][column];
			if(token == EToken.EMPTY)
				return true;
		}
		return false;
	}
	
	//Determines if a 'pop' move is legal or not
	private boolean canPop(EToken[][] board, int column, boolean isMax){
		if(column < 0 || column > board[0].length)
			return false;
		if(isMax ? this.ai_meHasPopped : !this.ai_opponentHasPopped == isMax)
			return false;
		EToken bottomToken = board[board.length-1][column];
		return bottomToken == (isMax ? EToken.ME : EToken.OPPONENT);
	}
	
	//Executes 'place' move
	private EToken[][] place(EToken[][] originalBoard, int column, boolean isMax){
		int index = originalBoard.length-1;
		while(originalBoard[index][column]!=EToken.EMPTY)
			index++;
		
		EToken[][] newBoard = new EToken[originalBoard.length][originalBoard[0].length];
		for(int i=0; i<originalBoard.length; i++){
			newBoard[i] = originalBoard[i].clone();
		}
		
		newBoard[index][column] = (isMax ? EToken.ME : EToken.OPPONENT);
		return newBoard;
	}
	
	//Executes 'pop' move
	private EToken[][] pop(EToken[][] originalBoard, int column, boolean isMax){
		EToken[][] newBoard = new EToken[originalBoard.length][originalBoard[0].length];
		for(int i=0; i<originalBoard.length; i++){
			newBoard[i] = originalBoard[i].clone();
		}
		
		for(int i=newBoard.length; i>=1; i--){
			newBoard[i][column] = newBoard[i-1][column];
		}
		newBoard[0][column] = EToken.EMPTY;
		
		if(isMax)
			ai_meHasPopped = true;
		else
			ai_opponentHasPopped = true;
		
		return newBoard;
	}
	
	//TODO: [Fiona]
	private int eval(EToken[][] board){
		
		return 0;
	}

	public static void main(String[] args){
		new ConnectNAI();
	}
	
	private enum EToken {
		ME,
		OPPONENT,
		EMPTY
	}
}
