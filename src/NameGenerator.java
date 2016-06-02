import java.util.Random;

public class NameGenerator {
	private int currentIndex;
	private static final String[] NAME_TABLE = new String[] { "a", "b", "c",
			"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
			"q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	public NameGenerator() {
		reset();
	}

	public NameGenerator(int startIndex) {
		this.currentIndex = startIndex;
	}

	protected static String getName(int index) {
		
		Random rnd = new Random();
		
		final int length = NAME_TABLE.length;
		int rand = rnd.nextInt(16) + 4;
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < rand; ++i)
		{
			sb.append(NAME_TABLE[rnd.nextInt(length)]);
		}
		
		return sb.toString();
	}

	public void reset() {
		currentIndex = 0;
	}

	public String next() {
		return getName(currentIndex++);
	}

	public String current() {
		return currentIndex == 0 ? null : getName(currentIndex - 1);
	}
}
