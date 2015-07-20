

public class Teste {

	public static void main(String[] args) {
		int sizeX = 5;
		for (int load = 1; load <= 10; load++) {
			System.out.println("X: " + Math.floorMod(load-1, sizeX));
			System.out.println("Y: " + Math.floorDiv(load-1, sizeX));
		}
		
		String s = "Colunas por linha:;;5;;;;";
		
		String[] cols = s.split(";");
		for (String col : cols) {
			System.out.println(col);
		}
	}
}
