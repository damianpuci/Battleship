import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.codec.digest.DigestUtils;


public class Client {
	static ServerSocket serverSocket;
	static Socket opponentSocket;
	static InputStream is;
	static ObjectInputStream ois;
	static OutputStream os;
	static ObjectOutputStream oos;
	static GameBoard gm = new GameBoard();

	static boolean breaking=false;
	static boolean end_game=false;

	static String opponentSHA1;
	static String mySHA1;

	public static final String DRIVER = "org.sqlite.JDBC";
	public static final String DB_URL = "jdbc:sqlite:Battleship.db";

	private static Connection conn;
	private static Statement stat;

	public Client(){

	}

	public static boolean createTable()  {
		String createGameBoard = "CREATE TABLE IF NOT EXISTS GameBoard (id INTEGER PRIMARY KEY AUTOINCREMENT, saving_name varchar(255),boardArray varchar(255), fieldsLeft int )";

		try {
			stat.execute(createGameBoard);

		} catch (SQLException e) {
			System.err.println("Error during creating table.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean insertGameBoard(String filename){
		try {
			PreparedStatement prepStmt = conn.prepareStatement(
					"insert into GameBoard values (NULL,?, ?, ?);");
			prepStmt.setString(1, filename);
			prepStmt.setString(2, gm.saveToString());
			prepStmt.setInt(3, gm.fieldsLeft);
			prepStmt.execute();
		} catch (SQLException e) {
			System.err.println("Blad przy wstawianiu czytelnika");
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public static GameBoard loadFromString(String serObj) {
		GameBoard gm=new GameBoard();
		try {
			byte b[] = serObj.getBytes(); 
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			gm = (GameBoard) si.readObject();

		} catch (Exception e) {
			System.out.println(e);
		}
		return gm;
	}


	public static String selectGameBoard(String savingName) {
		String gameBoard="";
		try {
			String query = "SELECT boardArray FROM GameBoard WHERE saving_name LIKE '%"+ savingName + "'";
			ResultSet result =stat.executeQuery(query);
			gameBoard = result.getString("boardArray");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return gameBoard;
	}


	public static void closeConnection1() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Problem with closing connection");
			e.printStackTrace();
		}
	}

	static void startServer(int port)throws IOException {
		serverSocket = new ServerSocket(port);	
	}

	static void connectToServer(String ip, int port) throws UnknownHostException, IOException {
		opponentSocket = new Socket(ip,port);
		is = opponentSocket.getInputStream();
		os = opponentSocket.getOutputStream();
		oos = new ObjectOutputStream(os);
		ois = new ObjectInputStream(is);
	}

	static void waitForClient() throws IOException {
		opponentSocket = serverSocket.accept();
		is = opponentSocket.getInputStream();
		os = opponentSocket.getOutputStream();
		oos = new ObjectOutputStream(os);
		ois = new ObjectInputStream(is);
	}

	static Message getMessageFromOpponent() throws IOException, ClassNotFoundException {
		Message msg = (Message)ois.readObject();
		return msg;
	}

	static void sendMessageToOpponent(Message msg) throws IOException {
		oos.writeObject(msg);
	}

	void closeConnection() throws IOException {
		serverSocket.close();
		opponentSocket.close();
		is.close();
		os.close();
		oos.close();
		ois.close();
	}
	public static void main(String[] argc){
		try {
			Class.forName(Client.DRIVER);
		} catch (ClassNotFoundException e) {
			System.err.println("There is no JDBC driver.");
			e.printStackTrace();
		}

		try {
			conn = DriverManager.getConnection(DB_URL);
			stat = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Problem with opening connection.");
			e.printStackTrace();
		}
		createTable();



		String[][] pola=new String[10][10];
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				pola[i][j]=(char)(65+j)+String.valueOf(i+1);
			}
		}

		Scanner input = new Scanner(System.in);

		System.out.println("1. Graj.");
		System.out.println("2. Wczytaj gre.");
		System.out.println("3. Zakoncz gre.");
		String choice_str1=input.nextLine();
		int wybor=Integer.parseInt(choice_str1);
		boolean load_flag=false;
		if(wybor==2) load_flag=true;
		if(wybor!=3){

			System.out.println("1. Stworz serwer.");
			System.out.println("2. Polacz z serwerem.");
			System.out.println("3. Zakoncz gre.");

			String choice=input.nextLine();
			int choice_int=Integer.parseInt(choice);

			switch(choice_int){
			case(1):
				System.out.println("Podaj numer portu:");
			int port;
			String port_str=input.nextLine();
			port=Integer.parseInt(port_str);
			try{
				startServer(port);
			}
			catch(Exception e){
				System.out.println(e);
			}
			try{
				waitForClient();
			}
			catch(Exception e){
				System.out.println(e);
			}
			break;
			case(2):
				System.out.println("Podaj numer portu:");
			int port2;
			String port_str2=input.nextLine();
			port2=Integer.parseInt(port_str2);
			System.out.println("Podaj ip serwera:");
			String ip=input.nextLine();
			try{
				connectToServer(ip, port2);
			}
			catch(Exception e){
				System.out.println(e);
			}
			break;
			case(3):
				System.exit(0);
			}
			int x=0;
			int y=0;

			if(load_flag==false){
				ArrayList<Ship> list=new ArrayList<Ship> ();
				list.add(new Ship("Cruiser",ShipType.Cruiser));
				//list.add(new Ship("Destroyer",ShipType.Destroyer));
				//list.add(new Ship("Submarine",ShipType.Submarine));
				String text;
				String text2;

				for(Ship s : list){
					do{
						if(s.type==ShipType.Submarine){
							System.out.println("Prosze podac pole, na ktorym bedzie sie znajdowac statek Submarine\n");
							text2 = input.nextLine();
							text="pionowa";
						}
						else{
							System.out.println("Prosze podac orientacje statku " + s.name + " (pionowa/pozioma).\n");
							text = input.nextLine();
							System.out.println("Prosze podac pole, na ktorym bedzie sie znajdowac poczatek statku np. B3 (przy orientacji pionowej gorny wierzcholek, przy orientacji poziomej lewy wierzcholek)\n");
							text2 = input.nextLine();
						}	
						x=-1;
						y=-1;

						for(int i=0;i<10;i++){
							for(int j=0;j<10;j++){
								if(pola[i][j].equals((String)text2)){
									x=i;
									y=j;

								}
							}
						}


					}
					while(gm.putShip(s.type, text, x, y)==false);


				}

				System.out.println(GameBoardPrinter.getBoardString(gm));
			}
			else{
				System.out.println("Podaj nazwe wczytywanego zapisu:");
				String name=input.nextLine();
				gm=loadFromString(selectGameBoard(name));			
			}
			mySHA1=DigestUtils.sha1Hex(gm.convertBoardToString());
			Message msg_1=new Message(MsgType.READY, mySHA1);
			try{
				sendMessageToOpponent(msg_1);
			}
			catch(Exception e){
				System.out.println(e);
			}
			Message msg_2=new Message();
			try{
				msg_2=getMessageFromOpponent();
			}
			catch(Exception e){
				System.out.println(e);
			}
			opponentSHA1=msg_2.msgContent;



			if(choice_int==1){
				String type="";
				while(true){
					System.out.println("czat(C), strzelaj(CO), zapisz gre(S), zakoncz gre(E))");
					type=input.nextLine();
					if (type.equals("C") || type.equals("CO") || type.equals("S") || type.equals("E")) break;
				}

				if(type.equals("E")){
					breaking = true;
				}
				String content="";
				if(type.equals("C") || type.equals("CO")){
					System.out.println("Podaj tresc wiadomosci:");
					content=input.nextLine();
				}

				MsgType typ=MsgType.CHAT;
				Message msg1=new Message();
				if(type.equals("C")){
					typ=MsgType.CHAT;
					msg1=new Message(typ,content);
				}
				else if(type.equals("CO")){
					typ=MsgType.COORDINATES;
					msg1=new Message(typ,content);
				}
				else if(type.equals("RESULTS")){
					typ=MsgType.RESULTS;

					msg1=new Message(typ,gm);

				}
				else if(type.equals("S")) {
					typ=MsgType.SAVE_GAME;
					System.out.println("Podaj nazwe zapisu:");
					String filename=input.nextLine();
					insertGameBoard(filename);
					msg1=new Message(typ,"");
					closeConnection1();
				}

				try{
					sendMessageToOpponent(msg1);
				}
				catch(Exception e){
					System.out.println("exception");
				}

			}



			while(true){
				Message msg2=new Message();

				try{
					msg2=getMessageFromOpponent();
				}
				catch(Exception e){
					System.out.println("Error while receiving message.");
				}
				if(msg2.msgType==MsgType.RESULTS2){
					System.out.println("Plansza przeciwnika wygladala nastepujaco:");

					System.out.println(GameBoardPrinter.getBoardString(msg2.g));
					System.out.println(msg2.g.convertBoardToString());
					System.out.println("Przeciwnik wyszedl z gry, wygrales walkowerem!");
					MsgType m=MsgType.RESULTS2;

					Message msg=new Message(m,gm);
					try{
						sendMessageToOpponent(msg);
					}
					catch(Exception e){
						System.out.println("exception");
					}

					break;
				}
				else if(msg2.msgType==MsgType.SAVE_GAME){
					System.out.println("Przeciwnik chce zapisac gre, podaj nazwe zapisu");
					String filename=input.nextLine();
					insertGameBoard(filename);
					closeConnection1();
					break;
				}
				else if(msg2.msgType==MsgType.COORDINATES){
					String coordinate=msg2.msgContent;
					x=0;
					y=0;

					for(int i=0;i<10;i++){
						for(int j=0;j<10;j++){
							if(pola[i][j].equals((String)coordinate)){
								x=i;
								y=j;

							}
						}
					}
					if(gm.shoot(x,y)==true){
						if(gm.anyShipsLeft()==false){
							System.out.println("Przeciwnik trafil w Twoj statek.");
							System.out.println(GameBoardPrinter.getBoardString(gm));
							MsgType typee=MsgType.WON;
							Message last_msg=new Message(typee,gm);
							try{
								sendMessageToOpponent(last_msg);
							}
							catch(Exception e){
								System.out.println("exception");
							}

						}
						else{
							System.out.println("Przeciwnik trafil w Twoj statek.");
							System.out.println(GameBoardPrinter.getBoardString(gm));

							MsgType typee=MsgType.WAS_HIT;
							Message last_msg=new Message(typee,"");
							try{
								sendMessageToOpponent(last_msg);
							}
							catch(Exception e){
								System.out.println("Cannot send message.");
							}


						}
						continue;
					}
					else{
						System.out.println("Przeciwnik chybil.");
					}

					System.out.println(GameBoardPrinter.getBoardString(gm));
				}
				else if(msg2.msgType==MsgType.CHAT){
					System.out.println(msg2.msgContent);
				}
				else if(msg2.msgType==MsgType.WON){
					System.out.println("Wygrales!");
					System.out.println("Plansza przeciwnika wygladala nastepujaco:");
					System.out.println(GameBoardPrinter.getBoardString(msg2.g));
					System.out.print(DigestUtils.sha1Hex(msg2.g.convertBoardToString())+ " =? ");
					System.out.println(opponentSHA1);
					MsgType typee=MsgType.LOST;

					Message last_msg=new Message(typee,gm);
					try{
						sendMessageToOpponent(last_msg);
					}
					catch(Exception e){
						System.out.println("exception");
					}
					break;
				}
				else if(msg2.msgType==MsgType.WAS_HIT){
					System.out.println("Trafiles przeciwnika, strzelaj jeszcze raz.");
				}

				else if(msg2.msgType==MsgType.LOST){
					System.out.println("Plansza przeciwnika wygladala nastepujaco:");
					System.out.println(GameBoardPrinter.getBoardString(msg2.g));
					System.out.print(DigestUtils.sha1Hex(msg2.g.convertBoardToString())+ "=?");
					System.out.println(opponentSHA1);
					break;
				}

				String type="";
				while(true){
					System.out.println("czat(C), strzelaj(CO), zapisz gre(S), zakoncz gre(E))");
					type=input.nextLine();
					if (type.equals("C") || type.equals("CO") || type.equals("S") || type.equals("E")) break;
				}

				if(type.equals("E")){
					breaking = true;
				}
				String content="";
				if(type.equals("C") || type.equals("CO")){
					System.out.println("Podaj tresc wiadomosci:");
					content=input.nextLine();
				}

				MsgType typ=MsgType.CHAT;
				Message msg1=new Message();
				if(type.equals("C")){
					typ=MsgType.CHAT;
					msg1=new Message(typ,content);
				}
				else if(type.equals("CO")){
					typ=MsgType.COORDINATES;
					msg1=new Message(typ,content);
				}
				else if(type.equals("RESULTS")){
					typ=MsgType.RESULTS;

					msg1=new Message(typ,gm);

				}
				else if(type.equals("S")) {
					typ=MsgType.SAVE_GAME;
					System.out.println("Podaj nazwe zapisu.");
					String filename=input.nextLine();
					insertGameBoard(filename);
					closeConnection1();
					msg1=new Message(typ,"");
				}

				try{
					sendMessageToOpponent(msg1);
				}
				catch(Exception e){
					System.out.println("Cannot send message.");
				}
				if(type.equals("S")){
					break;
				}
			}


			if(breaking==true){
				MsgType m=MsgType.RESULTS2;

				Message msg=new Message(m,gm);
				try{
					sendMessageToOpponent(msg);
				}
				catch(Exception e){
					System.out.println("Cannot send message.");
				}
				System.out.println("Plansza przeciwnika wygladala nastepujaco:");
				System.out.println(msg.g.convertBoardToString());

				Message msg2=new Message();

				try{
					msg2=getMessageFromOpponent();
				}
				catch(Exception e){
					System.out.println("Cannot receive message.");
				}
				System.out.println(GameBoardPrinter.getBoardString(msg2.g));

				input.close();
			}


		}
		else if (wybor==3){
			input.close();
			System.exit(0);
		}


	}
}
