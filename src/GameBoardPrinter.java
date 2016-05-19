
public class GameBoardPrinter {
	public GameBoardPrinter() {
		
	}
	
	static public String getBoardString(GameBoard gameBoard) {
		String result="";
		result+="  ABCDEFGHIJ\n";
		for(int i=0;i<10;i++){
			if(i!=9){
			result+=(i+1)+" ";
			}
			else result+=(i+1);
			for(int j=0;j<10;j++){
				if(gameBoard.boardArray[i][j]==0){
					result+="~";
				}
				else if(gameBoard.boardArray[i][j]==1){
				    result+="S";
				}
				else result+="X";
			}
			result+="\n";
		}
		return result;
	}
}
