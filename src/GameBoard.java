
import java.io.ByteArrayOutputStream;

import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameBoard implements Serializable{
	public int id;
	public String saving_name="";
	protected int[ ][ ] boardArray = new int[10][10];
	int fieldsLeft;
	public GameBoard(){
		fieldsLeft=0;
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				boardArray[i][j]=0;
			}
		}

	}
	
	public String saveToString() {
        String result = "";
        try {
           ByteArrayOutputStream bo = new ByteArrayOutputStream();
           ObjectOutputStream so = new ObjectOutputStream(bo);
           so.writeObject(this);
           so.flush();
           result = bo.toString();
       } catch (Exception e) {
           System.out.println(e);
       }
       return result;
    }



    
	
	private boolean putShipPart(int x, int y) {
		if(x>9 || x<0 || y>9 || y<0){
			System.out.println("Wyszedles poza plansze gry");
			return false;
		}
		else{
			if(boardArray[x][y]==0){
				boardArray[x][y]=1;
				fieldsLeft++;
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	
	public boolean anyShipsLeft(){
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				if(boardArray[i][j]==1){
					return true;
				}
			}
		}
		return false;
	}
	
	
//	public boolean checkField(int x, int y){
//		if(boardArray[x][y]==1){
//			return true;
//		}
//		else return false;
//	}
	public boolean shoot(int i, int j){
		if(boardArray[i][j]==1){
			boardArray[i][j]=2;
			return true;
		}
		return false;
	}
	
	public boolean putShip(ShipType ship, String orientation, int x, int y ){
		if(ship==ShipType.Cruiser){
			if(orientation.equals("pionowa")){
				if(x>9 || x<0 || x+1>9 || x+1<0 || x+2>9 || x+2<0 || y<0 || y>9){
					System.out.println("Wyszedles poza plansze gry");
					return false;
				}
				else{
					for(int i=0;i<3;i++){
						if(boardArray[x+i][y]==0){
							putShipPart(x+i,y);	
						}
						else{
							System.out.println("Na zadanych polach znajduje sie juz inny statek!");
							return false;
						}
					}
					return true;
				}
			}
			

			else if(orientation.equals("pozioma")){
				if(x>9 || x<0 || y+1>9 || y+1<0 || y+2>9 || y+2<0 || y<0 || y>9){
					System.out.println("Wyszedles poza plansze gry");
					return false;
				}
				else{
					
						for(int i=0;i<3;i++){
							if(boardArray[x][y+i]==0){
								putShipPart(x,y+i);
							}
							else{
								System.out.println("Na zadanych polach znajduje sie juz inny statek!");
								return false;
							}
						}
					
					
					return true;
					
				}
				
			}
			
		}
		else if(ship==ShipType.Destroyer){
			if(orientation.equals("pionowa")){
				if(x>9 || x<0 || x+1>9 || x+1<0 || y<0 || y>9){
					System.out.println("Wyszedles poza plansze gry");
					return false;
				}
				else{
					
					for(int i=0;i<2;i++){
						if(boardArray[x+i][y]==0){
							putShipPart(x+i,y);
						}
						else{
							System.out.println("Na zadanych polach znajduje sie juz inny statek!");
							return false;
						}
					}
					return true;
			
				}
			}
			else if(orientation.equals("pozioma")){
				if(x>9 || x<0 || y+1>9 || y+1<0 || y<0 || y>9){
					System.out.println("Wyszedles poza plansze gry");
					return false;
				}
				else{
					for(int i=0;i<2;i++){
						if(boardArray[x][y+i]==0){
						putShipPart(x,y+i);
						}
						else{
							System.out.println("Na zadanych polach znajduje sie juz inny statek!");
							return false;
						}
					}
				return true;
				}
			}
		}
		else if(ship==ShipType.Submarine){
			if(x>9 || x<0 || y<0 || y>9){
				System.out.println("Wyszedles poza plansze gry");
				return false;
			}
			else{
				if(boardArray[x][y]==0){
					putShipPart(x,y);
				}
				else{
					System.out.println("Na zadanym polu znajduje sie juz inny statek!");
					return false;
				}
				return true;
			}
		
		}
		
		return false;
	}
	
	public String convertBoardToString(){
		String board="";
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				if(boardArray[i][j]==1 || boardArray[i][j]==2){
					board+=i;
					board+=j;
				}
			}
		}
		return board;
	}
	
}
	
	
	
	
