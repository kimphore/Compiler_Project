import java.io.IOException;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.Method;

public class Obfuscator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JavaClass inputClass = null;
		try {
			inputClass = Repository.lookupClass("Test");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ClassGen cg = new ClassGen(inputClass);
		/*
		 * Obfuscator Func
		 */
		
		Renamer p = new Renamer();
		
		p.ExcuteMethodRename(cg);
		p.ExcuteFieldRename(cg);
		
		BranchAdder ba = new BranchAdder();
		
		ba.ExcuteBrandAdder(cg);
		
		try {
			cg.getJavaClass().dump("AfterClass.class");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
