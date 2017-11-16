
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient {
	private static BoardMessage currentBoard;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static Scanner kb;

	private static Runnable listener = () -> {
		try {
			while ( ois.available() != 0 ){
				Object o = ois.readObject();
				if ( o instanceof ErrorMessage ){
					ErrorMessage e = (ErrorMessage)o;
					System.out.println("Error: " + e.getError());
				} else if ( o instanceof BoardMessage ){
					BoardMessage b = (BoardMessage)o;
					currentBoard = b;
					printBoard();
					MoveMessage m = makeMove();
					oos.writeObject(m);
				} else {
					System.out.println("Unknown object received: " + o);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	};
	
	
	public static void main(String[] args){
		try (Socket socket = new Socket("18.221.102.182", 38006)) {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
			kb = new Scanner(System.in);

			System.out.print("Enter a name: ");
			String user = kb.nextLine();

			Message connect = new ConnectMessage(user);
			oos.writeObject(connect);

			Message newGameCommand = new CommandMessage(CommandMessage.Command.NEW_GAME);
			oos.writeObject(newGameCommand);
			
			Thread listenerThread = new Thread(listener);
			listenerThread.start();
			
		} catch ( Exception e ){
			e.printStackTrace();
		} finally {
			System.out.println("Disconnected from server.");
		}
	}

	public static void printBoard(){
		byte[][] grid = currentBoard.getBoard();
		for ( int i=0; i<grid.length; i++){
			for ( int j=0; j<grid.length; j++){
				String mark = " ";
				if ( grid[i][j] == 1 ){
					mark = "X";
				} else if ( grid[i][j] == 2 ){
					mark = "O";
				}
				System.out.printf("[%s]", mark);
			}
			System.out.println();
		}
	}

	public static MoveMessage makeMove(){
		boolean done = false;
		int row = 0,col = 0;
		while ( !done ){
			System.out.print("Choose a move. Enter row and column, separated by spaces: ");
			row = kb.nextInt();
			col = kb.nextInt();
			byte[][] grid = currentBoard.getBoard();
			if ( row >= 0 && col >= 0 && row < 3 && col < 3 && grid[row][col] == 0 ){
				done = true;
			}
		}
		return new MoveMessage((byte)row,(byte)col);
	}
}
