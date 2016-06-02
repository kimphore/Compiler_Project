import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Field;


public class Renamer {
	NameGenerator nameGen = new NameGenerator(1500);
	
	public void ExcuteMethodRename(ClassGen cg)
	{
		System.out.println("<Method Renamer Start>");
		//cg가 넘어옴
		for(Method m : cg.getMethods())
		{
			if(m.isNative() || m.getName().equals("<clinit>") || m.getName().equals("<init>")
					||m.getName().equals("main"))
				continue;
			
			String oldName = m.getName();
			String newName = nameGen.next();
			
			System.out.println(oldName + "->" + newName);
			
			int utf8 = m.getNameIndex();
			
			if(utf8 > -1)
			{
				ConstantUtf8 utf = (ConstantUtf8)cg.getConstantPool().getConstant(utf8);
				//Method이름을 가져와서,
				if(utf.getBytes().equals(oldName))
					utf.setBytes(newName);
				//새로운 이름으로 셋.				
			}
			fixClass(cg, cg.getClassName(), oldName, newName, m);
		}
		System.out.println("<Method Renamer End>");
	}
	
	public void ExcuteFieldRename(ClassGen cg)
	{
		System.out.println("<Field Renamer Start>");
		//필드가 뭐지;
		for(Field field : cg.getFields())
		{
			ConstantPoolGen cpg = cg.getConstantPool();
			String oldName = field.getName();
			String newName = nameGen.next();
			int index = field.getNameIndex();
			int newIndex = cpg.addUtf8(newName);
			
			cpg.setConstant(index, cpg.getConstant(newIndex));
			System.out.println(oldName + "->" + newName);
			
			int fieldRef = cg.getConstantPool().lookupFieldref(cg.getClassName(), oldName, field.getSignature());
			
			if(fieldRef > -1)
			{
				ConstantFieldref ref = (ConstantFieldref)cg.getConstantPool().getConstant(fieldRef);
				ConstantNameAndType type = (ConstantNameAndType)cg.getConstantPool().getConstant(ref.getNameAndTypeIndex());
				
				
				int nameIndex = type.getNameIndex();
				
				if(nameIndex > -1)
				{
					ConstantUtf8 utf8 = (ConstantUtf8)cpg.getConstant(nameIndex);
					if(utf8 != null)
					{
						utf8.setBytes(newName);
					}
				}
			}
			
		}
		System.out.println("<Field Renamer End>");
	}
	
	private void fixClass(ClassGen cg, String className, String oldName, String newName, Method m)
	{
		int index = cg.getConstantPool().lookupMethodref(className, oldName, m.getSignature());
		//Method Index찾고
		
		if(index > -1)
		{
			ConstantMethodref ref = (ConstantMethodref)cg.getConstantPool().getConstant(index);
			
			int typeIndex = ref.getNameAndTypeIndex();
			
			if(typeIndex > -1)
			{
				ConstantNameAndType nameType = (ConstantNameAndType)cg.getConstantPool().getConstant(typeIndex);
				
				int utf8 = nameType.getNameIndex();
				
				if(utf8 > -1)
				{
					ConstantUtf8 utf = (ConstantUtf8)cg.getConstantPool().getConstant(utf8);
					
					if(utf.getBytes().equals(oldName))
						utf.setBytes(newName);
				}
			}
		}
	}
}
