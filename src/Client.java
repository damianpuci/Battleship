import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {
	static ServerSocket serverSocket;
	static Socket opponentSocket;
	static InputStream is;
	static ObjectInputStream ois;
	static OutputStream os;
	static ObjectOutputStream oos;
	static boolean breaking=false;
	static GameBoard gm = new GameBoard();
	static GameBoard opponent_gm=new GameBoard();
	static boolean end_game=false;

	public Client(){

	}

	static boolean saveToFile(String filename){

		PrintWriter zapis;
		try{
			zapis = new PrintWriter(filename);
		}
		catch (Exception e){
			System.out.println(e);
			return false;
		}
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				zapis.print(gm.boardArray[i][j]+" ");
			}
			zapis.println();
		}

		//zapis.println(GameBoardPrinter.getBoardString(opponent_gm));
		zapis.close();
		return true;
	}

	static void loadFromFile(String filename){
		Scanner odczyt = null;
		try{
			odczyt = new Scanner(new File(filename));
		}
		catch (Exception e){
			System.out.println(e);
		}
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				gm.boardArray[i][j]=odczyt.nextInt();
			}
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

		String[][] pola=new String[10][10];
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				pola[i][j]=(char)(65+j)+String.valueOf(i+1);
			}
		}

		Scanner input = new Scanner(System.in);

		System.out.println("1. Graj");
		System.out.println("2. Wczytaj gre");
		System.out.println("3. Zakoncz gre");
		String choice_str1=input.nextLine();
		int wybor=Integer.parseInt(choice_str1);
		boolean load_flag=false;
		if(wybor==2) load_flag=true;
		if(wybor!=3){

			System.out.println("1. Stworz serwer");
			System.out.println("2. Polacz z serwerem");
			System.out.println("3. Zakoncz gre");

			String choice=input.nextLine();
			int choice_int=Integer.parseInt(choice);

			switch(choice_int){
			case(1):
				System.out.println("Podaj numer portu");
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
				System.out.println("Podaj numer portu");
			int port2;
			String port_str2=input.nextLine();
			port2=Integer.parseInt(port_str2);
			System.out.println("Podaj ip");
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
							System.out.println("Prosze podac pole, na ktorym bedzie sie znajdowac poczatek statku(przy orientacji pionowej gorny wierzcholek, przy orientacji poziomej lewy wierzcholek)\n");
							text2 = input.nextLine();
						}	
						x=0;
						y=0;
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



				//gm.setGameBoard();
				System.out.println(GameBoardPrinter.getBoardString(gm));
			}
			else{
				System.out.println("Podaj nazwe wczytywanego pliku");
				loadFromFile(input.nextLine());
			}
			if(choice_int==1){
				Message msg_1=new Message(MsgType.READY, gm.convertBoardToString());
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
				System.out.println(msg_2.msgContent);
				while(true){

					System.out.println("Podaj typ wiadomosci (CHAT(C), COORDINATES(CO),ZAPISZ GRE(S), ZAKONCZ GRE(E))");
					String type=input.nextLine();
					if(type.equals("E")){
						breaking = true;
						break;
					}

					System.out.println("Podaj tresc wiadomosci");
					String content=input.nextLine();


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
						//content=GameBoardPrinter.getBoardString(gm);
						msg1=new Message(typ,gm);

					}
					else if(type.equals("S")) {
						typ=MsgType.SAVE_GAME;
						System.out.println("Podaj nazwe pliku");
						String filename=input.nextLine();
						saveToFile(filename);
						msg1=new Message(typ,"");
					}

					try{
						sendMessageToOpponent(msg1);
					}
					catch(Exception e){
						System.out.println("exception");
					}
					if(type.equals("S")){
						break;
					}

					Message msg2=new Message();

					try{
						msg2=getMessageFromOpponent();
					}
					catch(Exception e){
						System.out.println("nie odebralem wiadomosci");
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
						input.close();
						break;
					}
					else if(msg2.msgType==MsgType.SAVE_GAME){
						System.out.println("Przeciwnik chce zapisac gre, podaj nazwe pliku");
						String filename=input.nextLine();
						saveToFile(filename);
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
						if(gm.checkField(x, y)==true){
							System.out.println("Przeciwnik trafil w Twoj statek");
							if(gm.fieldsLeft==1){
								System.out.println("Przegrales");
								MsgType typee=MsgType.RESULTS;

								Message last_msg=new Message(typee,gm);
								try{
									sendMessageToOpponent(last_msg);
								}
								catch(Exception e){
									System.out.println("exception");
								}
								break;
							}
							else{
								while((gm.boardArray[x][y]==1 || gm.boardArray[x][y]==2) && gm.fieldsLeft!=0){
									if(gm.fieldsLeft==1){
										end_game=true;
										System.out.println("Przegrales");
										MsgType typee=MsgType.RESULTS;
										System.out.println(gm.fieldsLeft);
										Message last_msg=new Message(typee,gm);
										try{
											sendMessageToOpponent(last_msg);
										}
										catch(Exception e){
											System.out.println("exception");
										}
										break;
									}
									gm.checkField(x, y);
									MsgType typee=MsgType.CHAT;
									String contentt="Trafiles w statek przeciwnika!";
									Message last_msg=new Message(typee,contentt);
									try{
										sendMessageToOpponent(last_msg);
									}
									catch(Exception e){
										System.out.println("exception");
									}

									Message last_msg2=new Message();
									try{
										last_msg2=getMessageFromOpponent();
									}
									catch(Exception e){
										System.out.println("exception");
									}
									String coordinate2=last_msg2.msgContent;
									x=0;
									y=0;

									for(int i=0;i<10;i++){
										for(int j=0;j<10;j++){
											if(pola[i][j].equals((String)coordinate2)){
												x=i;
												y=j;

											}
										}
									}
								}
								
							}
							if(end_game==true){
								break;
							}
						}
						else{
							System.out.println("Przeciwnik chybil");
						}

						System.out.println(GameBoardPrinter.getBoardString(gm));

					}
					else if(msg2.msgType==MsgType.CHAT){
						System.out.println(msg2.msgContent);
					}
					else if(msg2.msgType==MsgType.RESULTS){
						System.out.println("Wygrales!");
						//MsgType typee=MsgType.RESULTS;

						/*Message last_msg=new Message(typee,gm);
						try{
							sendMessageToOpponent(last_msg);
						}
						catch(Exception e){
							System.out.println("exception");
						}*/
						break;
					}
				}
			}
			else{

				Message msg_2=new Message();
				try{
					msg_2=getMessageFromOpponent();
				}
				catch(Exception e){
					System.out.println(e);
				}
				System.out.println(msg_2.msgContent);
				Message msg_1=new Message(MsgType.READY, gm.convertBoardToString());
				try{
					sendMessageToOpponent(msg_1);
				}
				catch(Exception e){
					System.out.println(e);
				}
				while(true){
					Message msg2=new Message();

					try{
						msg2=getMessageFromOpponent();
					}
					catch(Exception e){
						System.out.println("Nie udalo sie odebrac wiadomosci");
					}
					if(msg2.msgType==MsgType.RESULTS2){
						System.out.println("Plansza przeciwnika wygladala nastepujaco:");

						System.out.println(GameBoardPrinter.getBoardString(msg2.g));
						System.out.println(msg2.g.convertBoardToString());
						System.out.println("Przeciwnik wyszedl z gry, wygrales walkowerem!");
						MsgType m=MsgType.RESULTS2;
						//String cnt=GameBoardPrinter.getBoardString(gm);
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
						System.out.println("Przeciwnik chce zapisac gre, podaj nazwe pliku");
						String filename=input.nextLine();
						saveToFile(filename);
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
						if(gm.checkField(x,y)==true){
							System.out.println("Przeciwnik trafil w Twoj statek");
							if(gm.fieldsLeft==1){
								System.out.println("Przegrales");
								MsgType typee=MsgType.RESULTS;
								//String contentt=GameBoardPrinter.getBoardString(gm);
								Message last_msg=new Message(typee,gm);
								try{
									sendMessageToOpponent(last_msg);
								}
								catch(Exception e){
									System.out.println("exception");
								}
								break;
							}

							else{
								while((gm.boardArray[x][y]==1 || gm.boardArray[x][y]==2) && gm.fieldsLeft!=0){
									if(gm.fieldsLeft==1){
										end_game=true;
										System.out.println("Przegrales");
										MsgType typee=MsgType.RESULTS;
										System.out.println(gm.fieldsLeft);
										//String contentt=GameBoardPrinter.getBoardString(gm);
										Message last_msg=new Message(typee,gm);
										try{
											sendMessageToOpponent(last_msg);
										}
										catch(Exception e){
											System.out.println("exception");
										}
										break;
									}
									
									gm.checkField(x, y);
									MsgType typee=MsgType.CHAT;
									String contentt="Trafiles w statek przeciwnika!";
									Message last_msg=new Message(typee,contentt);
									try{
										sendMessageToOpponent(last_msg);
									}
									catch(Exception e){
										System.out.println("exception");
									}

									Message last_msg2=new Message();
									try{
										last_msg2=getMessageFromOpponent();
									}
									catch(Exception e){
										System.out.println("exception");
									}
									String coordinate2=last_msg2.msgContent;
									x=0;
									y=0;

									for(int i=0;i<10;i++){
										for(int j=0;j<10;j++){
											if(pola[i][j].equals((String)coordinate2)){
												x=i;
												y=j;

											}
										}
									}
									
								}
								if(end_game==true){
									break;
								}
							}

						}
						else{
							System.out.println("Przeciwnik chybil");
						}

						System.out.println(GameBoardPrinter.getBoardString(gm));
					}
					else if(msg2.msgType==MsgType.CHAT){
						System.out.println(msg2.msgContent);
					}
					else if(msg2.msgType==MsgType.RESULTS){
						System.out.println("Wygrales!");
						MsgType typee=MsgType.RESULTS;
						//String contentt=GameBoardPrinter.getBoardString(gm);
						Message last_msg=new Message(typee,gm);
						try{
							sendMessageToOpponent(last_msg);
						}
						catch(Exception e){
							System.out.println("exception");
						}
						break;
					}

					System.out.println("Podaj typ wiadomosci (CHAT(C), COORDINATES(CO),ZAPISZ GRE(S), ZAKONCZ GRE(E))");
					String type=input.nextLine();
					if(type.equals("E")){
						breaking=true;
						break;
					}

					System.out.println("Podaj tresc wiadomosci");
					String content=input.nextLine();


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
						//content=GameBoardPrinter.getBoardString(gm);
						msg1=new Message(typ,gm);

					}
					else if(type.equals("S")) {
						typ=MsgType.SAVE_GAME;
						System.out.println("Podaj nazwe pliku");
						String filename=input.nextLine();
						saveToFile(filename);
						msg1=new Message(typ,"");
					}

					try{
						sendMessageToOpponent(msg1);
					}
					catch(Exception e){
						System.out.println("exception");
					}
					if(type.equals("S")){
						break;
					}
				}

			}
			if(breaking==true){
				MsgType m=MsgType.RESULTS2;
				//String cnt=GameBoardPrinter.getBoardString(gm);
				Message msg=new Message(m,gm);
				try{
					sendMessageToOpponent(msg);
				}
				catch(Exception e){
					System.out.println("exception");
				}
				System.out.println("Plansza przeciwnika wygladala nastepujaco:");
				System.out.println(msg.g.convertBoardToString());

				Message msg2=new Message();

				try{
					msg2=getMessageFromOpponent();
				}
				catch(Exception e){
					System.out.println("exception");
				}
				System.out.println(GameBoardPrinter.getBoardString(msg2.g));

				input.close();
			}

			/*else{
				System.out.println("Plansza przeciwnika wygladala nastepujaco:");


				Message msg2=new Message();

				try{
					msg2=getMessageFromOpponent();
				}
				catch(Exception e){
					System.out.println("exception");
				}
				System.out.println(msg2.msgContent);
				if(msg2.msgType==MsgType.RESULTS2){
					System.out.println("Przeciwnik wyszedl z gry, wygrales walkowerem");
				}
				input.close();
				}*/
	while(true){}
		}
		else if (wybor==3){
			input.close();
			System.exit(0);
		}


	}
}
