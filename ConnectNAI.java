import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	public ConnectNAI() throws Exception{
		this.meHasPopped = false;
		this.opponentHasPopped = false;
		this.manageRefereeInteractions();
	}
	
	// Handles all interaction between the referee and the AI
	private void manageRefereeInteractions() throws Exception{
		String name = "Pat and Fiona\n";
		//Give name to referee
		System.out.print(name);
		System.out.flush();

		//Get game metadata
		BufferedReader scanner = new BufferedReader(
	            new InputStreamReader(System.in));
		String input = scanner.readLine();
		int nameIndex = input.indexOf(name);
		int playerNumber = Integer.parseInt(input.substring(nameIndex-3,nameIndex-2));
		String[] metadata = scanner.readLine().split(" ");
		int boardHeight = Integer.parseInt(metadata[0]);
		int boardWidth = Integer.parseInt(metadata[1]);
		this.winningLength = Integer.parseInt(metadata[2]);
		int firstPlayer = Integer.parseInt(metadata[3]);
		this.timeLimit = Integer.parseInt(metadata[4]);

		//Create board
		this.gameBoard = new EToken[boardHeight][boardWidth];
		for(int col=0;col<this.gameBoard.length;col++){
			for(int row=0;row<this.gameBoard[col].length;row++){
				this.gameBoard[col][row] = EToken.EMPTY;
			}
		}

		//Create weight-board
		this.gameBoardWeight = this.assignWeight(boardWidth, boardHeight);

		if(playerNumber == firstPlayer){
			Move move = miniMax(this.gameBoard,ConnectNAI.DEPTH);
			this.gameBoard = move.getBoard();
			System.out.print(move.getInstructions());
			System.out.flush();
		}
		
		//while win condition
		while(true){			
			//Process Opponent Move
			String scannerInput = scanner.readLine();
			String[] opponentMove = scannerInput.split(" ");
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
			Move move = miniMax(this.gameBoard,ConnectNAI.DEPTH);
			this.gameBoard = move.getBoard();
			if(move.isPop())
				this.meHasPopped = true;
			System.out.print(move.getInstructions());
			System.out.flush();

			//Log if ai popped
			this.ai_meHasPopped = this.meHasPopped;

			//If ai won, quit
			if(move.getScore()==ConnectNAI.WIN)
				break;
		}

		scanner.close();
	}

	//Initializes mini-max process
	private Move miniMax(EToken[][] board, int depth){
		return this.max(new Move(board), Integer.MIN_VALUE, Integer.MAX_VALUE, ConnectNAI.DEPTH, ConnectNAI.DEPTH);
	}

	//Executes logic for 'max player' turn
	private Move max(Move move, int alpha, int beta, int currentDepth, int base){
		EToken[][] board = move.getBoard();

		//Leaf node
		if(currentDepth==0){
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
		EToken[][] temp = this.gameBoard;
		for(Move m:children){
			this.gameBoard = m.getBoard();
		}
		this.gameBoard = temp;

		//Determine best move
		int currentScore = Integer.MIN_VALUE;
		Move bestMove = null;
		Move minMove = null;
		for(Move candidateMove:children){
			minMove = min(candidateMove,alpha,beta,currentDepth-1,base);
			if(minMove.getScore() > currentScore){
				bestMove = minMove;
				currentScore = Math.max(currentScore, minMove.getScore());
			}
			alpha = Math.max(currentScore, alpha);

			//Prune
			if(alpha > beta){
				break;
			}
		}
		if(currentDepth == base)
			return bestMove;
		move.setScore(bestMove.getScore());
		return move;
	}

	//Executes logic for 'min player' turn
	private Move min(Move move, int alpha, int beta, int currentDepth, int base){
		Move maxMove = null;
		EToken[][] board = move.getBoard();

		if(currentDepth==0){
			move.setScore(eval(board));
			return move;
		}

		//Build array of potential moves
		ArrayList<Move> children = new ArrayList<Move>();
		for(int i=0; i<board[0].length; i++){
			if(canPlace(board,i,true))
				children.add(new Move(this.place(board,i,false),i,1));
			if(canPop(board,i,true))
				children.add(new Move(this.pop(board,i,false),i,0));
		}

		//Evaluate moves
		int currentScore = Integer.MAX_VALUE;
		Move bestMove = null;
		for(Move candidateMove:children){
			maxMove = max(candidateMove,alpha,beta,currentDepth-1,base);
			if(maxMove.getScore() < currentScore){
				bestMove = maxMove;
				currentScore = Math.min(currentScore, maxMove.getScore());
			}
			beta = Math.min(currentScore, beta);

			//Prune
			if(alpha > beta){
				break;
			}
		}
		if(currentDepth == base)
			return bestMove;
		move.setScore(bestMove.getScore());
		return move;
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
			index--;

		//Clones board
		EToken[][] newBoard = new EToken[originalBoard.length][originalBoard[0].length];
		for(int i=0; i<originalBoard.length; i++){
			newBoard[i] = originalBoard[i].clone();
		}

		if(isMax)
			newBoard[index][column] = EToken.ME;
		else
			newBoard[index][column] = EToken.OPPONENT;
		//newBoard[index][column] = (isMax ? EToken.ME : EToken.OPPONENT);
		return newBoard;
	}

	//Executes 'pop' move
	private EToken[][] pop(EToken[][] originalBoard, int column, boolean isMax){
		EToken[][] newBoard = new EToken[originalBoard.length][originalBoard[0].length];
		for(int i=0; i<originalBoard.length; i++){
			newBoard[i] = originalBoard[i].clone();
		}

		//Clones board
		for(int i=newBoard.length-1; i>=1; i--){
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

		if(totalEval < ConnectNAI.LOSE)
			return ConnectNAI.LOSE;

		return totalEval;
	}

	//diagonal scan right to left
	private int diagonalRightEval(EToken[][] board){
		boolean isOpp, isMine, containsSpace;
		int runningTotal= 0, myPoints = 0, oppPoints =0;
		for(int row=0; row < board.length - this.winningLength ; row++){
			for(int col=0; col < board[0].length - this.winningLength; col++){
				isMine = false;
				isOpp= false;
				myPoints=0;
				oppPoints=0;
				containsSpace = true;
				for(int i=0; i < this.winningLength; i++){
					if(board[row+i][col+i] == EToken.ME){
						isMine = true;
						myPoints += gameBoardWeight[row+i][col+i];
					}
					if(board[row+i][col+i] == EToken.OPPONENT){
						isOpp = true;
						oppPoints += gameBoardWeight[row+i][col+i];
					}

					if(board[row+i][col+i] == EToken.EMPTY){
						containsSpace = true;

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

				if(isMine && !containsSpace){
					runningTotal += ConnectNAI.WIN;
				}

				else if(isOpp && !containsSpace){
					runningTotal += ConnectNAI.LOSE;
				}
			}
		}
		return runningTotal;
	}

	//diagonal scan left to right
	private int diagonalLeftEval(EToken[][] board){
		boolean isOpp, isMine, containsSpace;
		int runningTotal= 0, myPoints = 0, oppPoints =0;
		for(int row=board.length-1; row >= this.winningLength ; row--){
			for(int col=0; col < board[0].length-this.winningLength; col++){
				isMine = false;
				isOpp= false;
				myPoints=0;
				oppPoints=0;
				containsSpace = true;
				for(int i=0; i < this.winningLength; i++){
					if(board[row-i][col+i] == EToken.ME){
						isMine = true;
						myPoints += gameBoardWeight[row-i][col+i];
					}
					if(board[row-i][col+i] == EToken.OPPONENT){
						isOpp = true;
						oppPoints += gameBoardWeight[row-i][col+i];
					}

					if(board[row-i][col+i] == EToken.EMPTY){
						containsSpace = true;

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

				if(isMine && !containsSpace){
					runningTotal += ConnectNAI.WIN;
				}

				else if(isOpp && !containsSpace){
					runningTotal += ConnectNAI.LOSE;
				}
			}
		}

		return runningTotal;
	}

	//horizontal scan
	private int horizontalEval(EToken[][] board){
		int vertical = board.length;
		int horizontal = board[0].length;
		int runningTotal = 0, myTotal = 0, oppTotal = 0;
		boolean isMyPoints, isOppPoints, containsSpace;

		for(int i = vertical-1; i > -1; i--){
			myTotal = 0;
			oppTotal = 0;
			isMyPoints = false;
			isOppPoints = false;
			containsSpace = false; 
			for(int j = 0; j < horizontal - winningLength; j++ )
				for(int k = j; k < winningLength+j; k++){
					if(board[i][k] == EToken.ME){
						isMyPoints = true;
						myTotal += gameBoardWeight[i][k];
					}
					if(board[i][k] == EToken.OPPONENT){
						isOppPoints = true;
						oppTotal -= gameBoardWeight[i][k];
					}
					if(board[i][k] == EToken.EMPTY){
						containsSpace = true;
					}
					if(isOppPoints && isMyPoints)
						break;
				}
			if(isOppPoints && !isMyPoints){
				runningTotal += oppTotal;
			}

			else if(isMyPoints && !isOppPoints){
				runningTotal +=  myTotal;
			}

			if(isMyPoints && !containsSpace){
				runningTotal += ConnectNAI.WIN;
			}

			else if(isOppPoints && !containsSpace){
				runningTotal += ConnectNAI.LOSE;
			}

		}

		return runningTotal;
	}

	//vertical scan
	private int verticalEval(EToken[][] board){
		int horizontal = board[0].length;
		int vertical = board.length;
		int runningTotal = 0, myTotal = 0, oppTotal = 0;
		boolean isMyPoints, isOppPoints, containsSpace;
		for(int col = horizontal-1; col >= 0; col--){
			isMyPoints = false;
			isOppPoints = false;
			containsSpace = true; 
			myTotal = 0;
			oppTotal = 0;

			for(int rowBase = 0; rowBase <= vertical - this.winningLength; rowBase++ )
				for(int row = rowBase; row < this.winningLength; row++){
					if(board[row][col] == EToken.ME){
						isMyPoints = true;
						myTotal += gameBoardWeight[row][col];
					}
					if(board[row][col] == EToken.OPPONENT){
						isOppPoints = true;
						oppTotal -= gameBoardWeight[row][col];
					}
					if(board[row][col] == EToken.EMPTY){
						containsSpace = true;
					}

					if(isOppPoints && isMyPoints)
						break;
				}
			if(isOppPoints && !isMyPoints){
				runningTotal += oppTotal;
			}

			else if(isMyPoints && !isOppPoints){
				runningTotal += myTotal;
			}

			if(isMyPoints && !containsSpace){
				runningTotal += ConnectNAI.WIN;
			}

			else if(isOppPoints && !containsSpace){
				runningTotal += ConnectNAI.LOSE;
			}
		}

		return runningTotal;
	}

	//Assigns weighted values to each space based on board size
	private int[][] assignWeight(int horizontal, int vertical){
		int[][] boardWeight = new int[vertical][horizontal];
		for(int row=0; row<=vertical/2; row++){
			for(int col=0; col<=horizontal/2; col++){
				boardWeight[row][col] = 3 + (int)Math.pow(row,2) + (int)Math.pow(col, 2);
				boardWeight[vertical - 1 - row][col] = 3 + (int)Math.pow(row,2) + (int)Math.pow(col, 2);
				boardWeight[row][horizontal - 1 - col] = 3 + (int)Math.pow(row,2) + (int)Math.pow(col, 2);
				boardWeight[vertical - 1 - row][horizontal - 1 - col] = 3 + (int)Math.pow(row,2) + (int)Math.pow(col, 2);
			}
		}
		return boardWeight;
	}

	public static void main(String[] args) throws Exception{
		new ConnectNAI();
	}
}
