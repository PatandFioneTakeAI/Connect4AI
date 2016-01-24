import java.util.ArrayList;
import java.util.Scanner;


public class ConnectNAI {
	
	private static final int DEPTH = 3;

	//Game tracker
	private boolean meHasPopped;
	private boolean opponentHasPopped;
	private EToken[][] gameBoard;
	private int winningLength;
	
	//AI tracker
	private boolean ai_meHasPopped;
	private boolean ai_opponentHasPopped;	
	private EToken[][] candidateBoard;
	private int[][] gameBoardWeight;
	private int timeLimit;

	public ConnectNAI(){
		this.meHasPopped = false;
		this.opponentHasPopped = false;
		this.manageRefereeInteractions();
	}
	
	// Handles all interaction between the referee and the AI
	private void manageRefereeInteractions(){
		String name = "Pat and Fiona";
		//Give name to referee
		System.out.println(name);
		
		//Get game metadata
		Scanner scanner = new Scanner(System.in);
		String input = scanner.nextLine();
		int nameIndex = input.indexOf(name);
		int playerNumber = Integer.parseInt(input.substring(nameIndex-3,nameIndex-2));
		int boardHeight = scanner.nextInt();
		int boardWidth = scanner.nextInt();
		this.winningLength = scanner.nextInt();
		int firstPlayer = scanner.nextInt();
		this.timeLimit = scanner.nextInt();
		
		//Create board
		this.gameBoard = new EToken[boardHeight][boardWidth];
		
		//Create weight-board
		this.gameBoardWeight = this.assignWeight(boardWidth, boardHeight);
		
		if(playerNumber == firstPlayer)
			miniMax(this.gameBoard,Integer.MIN_VALUE,Integer.MAX_VALUE,ConnectNAI.DEPTH);
		
		//.....Somewhere goes:
			this.miniMax(null,0,0,0);
			candidateBoard.notify();
		
		scanner.close();
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
	
	//Executes logic for 'min player' turn
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

	//Evaluates current board based on connectedness using values from boardWeight
	private int eval(EToken[][] board){
			
			//horizontal scan
			int totalEval = horizontalEval(board);

			//vertical scan
			totalEval += verticalEval(board);

			//diagonal (L to R B to T)
			totalEval += diagonalLeftEval(board);

			//diagonal (R to L B to T)
			totalEval += diagonalRightEval(board);

			return totalEval;
		}

	//diagonal scan right to left
	private int diagonalRightEval(EToken[][] board){
		boolean isOpp, isMine;
		int runningTotal= 0, myPoints = 0, oppPoints =0;
		for(int i= board[0].length; i > -1 ; i--){
			for(int j = board.length -1 ; j > -1; j--){
				isMine = false;
				isOpp= false;
				myPoints=0;
				oppPoints=0;
				
				for(int k=0; k < this.winningLength; k++){
					if(board[i-k][j-k] == EToken.ME){
						isMine = true;
						myPoints += gameBoardWeight[i-k][j-k];
					}
					if(board[i-k][j-k] == EToken.OPPONENT){
						isOpp = true;
						oppPoints += gameBoardWeight[i-k][j-k];
					}
					if(isMine && isOpp){
						break;
					}
				}
				if(isMine && !isOpp){
					runningTotal += myPoints;
				}
				else if(isOpp && !isMine){
					runningTotal += oppPoints;
				}
			}
		}
		return runningTotal;
	}

	//diagonal scan left to right
	private int diagonalLeftEval(EToken[][] board){
		boolean isOpp, isMine;
		int runningTotal= 0, myPoints = 0, oppPoints =0;
		for(int i=board[0].length; i > -1 ; i++){
			for(int j=0; j < board.length; j++){
				isMine = false;
				isOpp= false;
				myPoints=0;
				oppPoints=0;
				for(int k=0; k < this.winningLength; k++){
					if(board[i-k][j+k] == EToken.ME){
						isMine = true;
						myPoints += gameBoardWeight[i-k][j+k];
					}
					if(board[i-k][j+k] == EToken.OPPONENT){
						isOpp = true;
						oppPoints += gameBoardWeight[i-k][j+k];
					}
					if(isMine && isOpp){
						break;
					}
				}
				if(isMine && !isOpp){
					runningTotal += myPoints;
				}
				if(isOpp && !isMine){
					runningTotal += oppPoints;
				}
			}
		}

		return runningTotal;
	}

	//horizontal scan
	private int horizontalEval(EToken[][] board){
		int horizontal = board.length;
		int vertical = board[0].length;
		int runningTotal = 0, myTotal = 0, oppTotal = 0;
		boolean isMyPoints;
		boolean isOppPoints;
		for(int i = vertical-1; i > -1; i--){
			myTotal = 0;
			oppTotal = 0;
			isMyPoints = false;
			isOppPoints = false;
			for(int j = 0; j <= horizontal - winningLength; j++ )
				for(int k = j; k < winningLength; k++){
					//if board[i][k] == EToken.ME{
					isMyPoints = true;
					myTotal += gameBoardWeight[i][k];

					//if == EToken.OPP
					isOppPoints = true;
					oppTotal -= gameBoardWeight[i][k];
					if(isOppPoints && isMyPoints)
						break;
				}
			if(isOppPoints){
				runningTotal += oppTotal;
			}

			if(isMyPoints){
				runningTotal +=  myTotal;
			}

		}

		return runningTotal;
	}

	//vertical scan
	private int verticalEval(EToken[][] board){
		int horizontal = board.length;
		int vertical = board[0].length;
		int runningTotal = 0, myTotal = 0, oppTotal = 0;
		boolean isMyPoints;
		boolean isOppPoints;
		for(int i = horizontal-1; i > -1; i--){
			isMyPoints = false;
			isOppPoints = false;
			myTotal = 0;
			oppTotal = 0;
			
			for(int j = 0; j <= vertical - this.winningLength; j++ )
				for(int k = j; k < this.winningLength; k++){
					//if board[i][k] == EToken.ME{
					isMyPoints = true;
					myTotal += gameBoardWeight[i][k];

					//if == EToken.OPP
					isOppPoints = true;
					oppTotal -= gameBoardWeight[i][k];
					if(isOppPoints && isMyPoints)
						break;
				}
			if(isOppPoints){
				runningTotal += oppTotal;
			}

			if(isMyPoints){
				runningTotal += myTotal;
			}
		}

		return runningTotal;
	}

	//Assigns weighted values to each space based on board size
	private int[][] assignWeight(int horizontal, int vertical){
		int[][] boardWeight = new int[vertical][horizontal];
		if(horizontal % 2 == 0){
			int max_weight_index = horizontal/2;
			for(int i=0; i < max_weight_index; i++){
				boardWeight[0][i] = (i * 3) + 1;
				boardWeight[0][horizontal-i-1] = (i * 3) + 1;
			}
			boardWeight[0][max_weight_index] = (max_weight_index *3) + 1;
			boardWeight[0][max_weight_index -1] = (max_weight_index *3) + 1;
		}
		else{
			int max_weight_index = (int) (horizontal/2 -.5);
			for(int i=0; i < max_weight_index; i++){
				boardWeight[0][i] = (i * 3) + 1;
				boardWeight[0][horizontal-i-1] = (i * 3) + 1;
			}
			boardWeight[0][max_weight_index] = (max_weight_index *3) + 1;

		}
		
		if(vertical % 2 == 0){
			int max_weight_index = vertical/2;
			for(int i=0; i < max_weight_index; i++){
				boardWeight[i][0] = (i * 3) + 1;
				boardWeight[horizontal-i-1][0] = (i * 3) + 1;
			}
			boardWeight[max_weight_index][0] = (max_weight_index *3) + 1;
			boardWeight[max_weight_index -1][0] = (max_weight_index *3) + 1;
		}
		else{
			int max_weight_index = (int) (vertical/2 -.5);
			for(int i=0; i < max_weight_index; i++){
				boardWeight[i][0] = (i * 3) + 1;
				boardWeight[horizontal-i-1][0] = (i * 3) + 1;
			}
			boardWeight[max_weight_index][0] = (max_weight_index *3) + 1;

		}

		for (int i=1; i < horizontal-1; i++){
			for (int j=1; j< vertical-1; j++){
				boardWeight[j][i] = boardWeight[j][0] + boardWeight[0][i];
			}
		}

		return boardWeight;

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
