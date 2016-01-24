import java.util.ArrayList;
import java.util.Scanner;

public class ConnectNAI {
	
	private static final int DEPTH = 3;
	private static final int WIN = 10000;
	private static final int LOSE = -10000;

	//Game tracker
	private boolean meHasPopped;
	private boolean opponentHasPopped;
	private EToken[][] gameBoard;
	private int winningLength;
	
	//AI tracker
	private boolean ai_meHasPopped;
	private boolean ai_opponentHasPopped;
	private int[][] gameBoardWeight;
	private int timeLimit;
	
	public ConnectNAI(){
		this.meHasPopped = false;
		this.opponentHasPopped = false;
		this.manageRefereeInteractions();
	}
	
	private void printGameBoard(){
		for(int col=0;col<gameBoard.length;col++){
			for(int row=0;row<gameBoard[col].length;row++){
				if(gameBoard[col][row]==EToken.EMPTY)
					System.out.print("-");
				else if(gameBoard[col][row]==EToken.ME)
					System.out.print("X");
				else if(gameBoard[col][row]==EToken.OPPONENT)
					System.out.print("O");
			}
			System.out.println();
		}
		System.out.println("\n");
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
		
		if(playerNumber == firstPlayer){
			Move move = miniMax(this.gameBoard,Integer.MIN_VALUE,Integer.MAX_VALUE,ConnectNAI.DEPTH);
			this.gameBoard = move.getBoard();
			System.out.println(move.getInstructions());
			printGameBoard();
		}
		
		//while win condition
		while(true){			
			//Process Opponent Move
			String[] opponentMove = scanner.nextLine().split(" ");
			int opponentColumn = Integer.parseInt(opponentMove[0]);
			boolean isPop = opponentMove[1].equals("0");
			if(isPop){
				this.gameBoard = this.pop(this.gameBoard, opponentColumn, false);
				this.opponentHasPopped = true;
			}
			else
				this.gameBoard = this.place(this.gameBoard, opponentColumn, false);
			
			//If opponent won, quit
			if(this.eval(gameBoard) == ConnectNAI.LOSE)
				break;
			
			//Log if opponent Popped
			this.ai_opponentHasPopped = this.opponentHasPopped;
			
			//Run minimax
			Move move = miniMax(this.gameBoard,Integer.MIN_VALUE,Integer.MAX_VALUE,ConnectNAI.DEPTH);
			this.gameBoard = move.getBoard();
			if(move.isPop())
				this.meHasPopped = true;
			System.out.println(move.getInstructions());

			//Log if ai popped
			this.ai_meHasPopped = this.meHasPopped;
			
			//If ai won, quit
			if(move.getScore()==ConnectNAI.WIN)
				break;
		}
		
		scanner.close();
	}
	
	//Initializes mini-max process
	private Move miniMax(EToken[][] board, int alpha, int beta, int depth){
		return this.max(new Move(board), alpha, beta, -ConnectNAI.DEPTH, 0);
	}
	
	//Executes logic for 'max player' turn
	private Move max(Move move, int alpha, int beta, int depthGoal, int currentDepth){
		Move minMove = null;
		EToken[][] board = move.getBoard();
		
		if(depthGoal==currentDepth){
			move.setScore(eval(move.getBoard()));
			return move;
		}
		
		//Build array of potential moves
		ArrayList<Move> children = new ArrayList<Move>();
		for(int i=0; i<board[0].length; i++){
			if(canPlace(board,i,true))
				children.add(new Move(this.place(board,i,true),i,1));
			if(canPop(board,i,true))
				children.add(new Move(this.pop(board,i,true),i,0));
		}
		
		//Determine best move
		int currentScore = Integer.MIN_VALUE;
		Move bestMove = null;
		for(Move candidateMove:children){
			minMove = min(candidateMove,alpha,beta,depthGoal,currentDepth+1);
			if(minMove.getScore() > currentScore){
				bestMove = minMove;
				currentScore = Math.max(currentScore, minMove.getScore());
			}
			alpha = Math.max(currentScore, alpha);
			
			//Prune
			if(alpha > beta)
				return bestMove;
		}
		return bestMove;
	}
	
	//Executes logic for 'min player' turn
	private Move min(Move move, int alpha, int beta, int depthGoal, int currentDepth){
		Move maxMove = null;
		EToken[][] board = move.getBoard();
		
		if(depthGoal==currentDepth){
			move.setScore(eval(board));
			return move;
		}
		
		//Build array of potential moves
		ArrayList<Move> children = new ArrayList<Move>();
		for(int i=0; i<board[0].length; i++){
			if(canPlace(board,i,true))
				children.add(new Move(this.place(board,i,true),i,1));
			if(canPop(board,i,true))
				children.add(new Move(this.pop(board,i,true),i,0));
		}
		
		//Evaluate moves
		int currentScore = Integer.MAX_VALUE;
		Move bestMove = null;
		for(Move candidateMove:children){
			maxMove = max(candidateMove,alpha,beta,depthGoal,currentDepth+1);
			if(maxMove.getScore() < currentScore){
				bestMove = maxMove;
				currentScore = Math.min(currentScore, maxMove.getScore());
			}
			beta = Math.min(currentScore, beta);
			
			//Prune
			if(alpha > beta)
				return bestMove;
		}
		return bestMove;
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

			if(totalEval > ConnectNAI.WIN)
				return ConnectNAI.WIN;
			
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
}
