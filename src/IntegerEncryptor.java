import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.w3c.dom.ls.LSOutput;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

public class IntegerEncryptor {
	
	public void ExcuteEncryption(ClassGen cg)
	{
		System.out.println("<Integer Encryption Start>");
		
		ConstantPoolGen cpg = cg.getConstantPool();
		
		AddMethods(cg);
		
		for(Method m: cg.getMethods())
		{	
			MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
			
			String methodName = mg.getMethod().getName();
			
			if(methodName == "pow"||
				methodName == "Equal"||
				methodName == "NotEqual"||
				methodName == "Mul"||
				methodName == "ConvertValue"||
				methodName == "Div"||
				methodName == "Mod")
				continue;
			
			if(m.isAbstract() || m.isNative() || m.getName().equals("<init>"))
				continue;
			
			EncryptInteger(mg, cpg);
			InjectionMethods(cg, mg);
		}
				
		System.out.println("<Integer Encryption End>");
	}
	
	private void EncryptInteger(MethodGen mg, ConstantPoolGen cpg)
	{
		//인티저 인크립션
		//일단 눈에보이는 Integer(뭐 우리껀 Long이지만) 전부 암호화. 암호화방식은 바꿀수도있음 ㅇㅇ
		for(InstructionHandle ih: mg.getInstructionList().getInstructionHandles())
		{
			if(ih.getInstruction() instanceof LDC2_W)
			{
				//Long형 불러오는
				LDC2_W ldc = (LDC2_W) ih.getInstruction();
								
				ConstantLong c = (ConstantLong)cpg.getConstant(ldc.getIndex());
				
				c.setBytes(ConvertValue(ldc.getValue(cpg).longValue()));
				
			}
		}
	}
	
	private void AddMethods(ClassGen cg)
	{
		//Encryption했을때, Decryption해서 계산할 메소드가 몇개 필요함. 그것들 넣어주는거.
		ConstantPoolGen cpg = cg.getConstantPool();
		InstructionList iList = new InstructionList();
		
		
		//1.pow함수
		InstructionHandle ihTemp = null;
		
		ihTemp = iList.append(new LLOAD(2));//0
		iList.append(new LCONST(0));//1
		iList.append(new LCMP());//2
		iList.append(new IFLE(ihTemp));//3
		iList.append(new LLOAD(0));//4
		iList.append(new GOTO(ihTemp));//5
		iList.append(new LCONST(1));//6
		iList.append(new LSTORE(4));//7
		iList.append(new LCONST(0));//8
		iList.append(new LSTORE(6));//9
		iList.append(new GOTO(ihTemp));//10
		iList.append(new LLOAD(4));//11
		iList.append(new LLOAD(0));//12
		iList.append(new LMUL());//13
		iList.append(new LSTORE(4));//14
		iList.append(new IINC(6, 1));//15
		iList.append(new ILOAD(6));//16
		iList.append(new I2L());//17
		iList.append(new LLOAD(2));//18
		iList.append(new LCONST(1));//19
		iList.append(new LSUB());//20
		iList.append(new LCMP());//21
		iList.append(new IFLT(ihTemp));//22
		iList.append(new LLOAD(4));//23
		iList.append(new LRETURN());//24
		
		iList.setPositions();
		
		
		InstructionHandle ih = iList.findHandle(3);
		
		((IFLE)ih.getInstruction()).setTarget(iList.findHandle(10));
		
		ih = iList.findHandle(7);
		((GOTO)ih.getInstruction()).setTarget(iList.findHandle(11));
		
		ih = iList.findHandle(16);
		((GOTO)ih.getInstruction()).setTarget(iList.findHandle(28));
		
		ih = iList.findHandle(35);
		
		((IFLT)ih.getInstruction()).setTarget(iList.findHandle(19));
		
		MethodGen mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.LONG
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "pow", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		//2.ConvertValue함수(인크립션함수)
		InstructionFactory iFac = new InstructionFactory(cg, cpg);
		
		iList = new InstructionList();
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(new LSTORE(2));
		iList.append(new LCONST(0));
		iList.append(new LSTORE(4));
		iList.append(new ICONST(0));
		iList.append(new ISTORE(6));
		iList.append(new GOTO(ihTemp));//8 -> 56
		iList.append(new LLOAD(2));
		iList.append(new LCONST(1));
		iList.append(new LCMP());
		iList.append(new IFGE(ihTemp));//14->20
		iList.append(new GOTO(ihTemp));//17->63
		iList.append(new LLOAD(2));
		iList.append(new LDC2_W(69));
		iList.append(new LREM());
		iList.append(new LCONST(1));
		iList.append(new LCMP());
		iList.append(new IFNE(ihTemp));//27->47
		iList.append(new LLOAD(4));
		iList.append(new LDC2_W(69));
		iList.append(new BIPUSH((byte) 32));
		iList.append(new ILOAD(6));
		iList.append(new ISUB());
		iList.append(new I2L());
		
		InvokeInstruction invoke = iFac.createInvoke("Test", "pow", Type.LONG
				, new Type[] {Type.LONG, Type.LONG}, Constants.INVOKESTATIC);
		
		iList.append(invoke);
		iList.append(new LADD());
		iList.append(new LSTORE(4));
		iList.append(new LLOAD(2));
		iList.append(new LDC2_W(69));
		iList.append(new LDIV());
		iList.append(new LSTORE(2));
		iList.append(new IINC(6,1));
		iList.append(new ILOAD(6));
		iList.append(new BIPUSH((byte)33));
		iList.append(new IF_ICMPLT(ihTemp));//60->11
		iList.append(new LLOAD(4));
		iList.append(new LRETURN());
		
		iList.setPositions();
		
		ih = iList.findHandle(8);
		((GOTO)ih.getInstruction()).setTarget(iList.findHandle(56));
		
		ih = iList.findHandle(14);
		((IFGE)ih.getInstruction()).setTarget(iList.findHandle(20));
		
		ih = iList.findHandle(17);
		((GOTO)ih.getInstruction()).setTarget(iList.findHandle(63));
		
		ih = iList.findHandle(27);
		((IFNE)ih.getInstruction()).setTarget(iList.findHandle(47));
		
		ih = iList.findHandle(60);
		((IF_ICMPLT)ih.getInstruction()).setTarget(iList.findHandle(11));
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.LONG
				, new Type[] {Type.LONG}, new String[] {"a"}
				, "ConvertValue", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
		//3.연산함수들
		//Equal
		
		iList = new InstructionList();
		
		invoke = iFac.createInvoke("Test", "ConvertValue", Type.LONG
				, new Type[] {Type.LONG}, Constants.INVOKESTATIC);
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(invoke);
		iList.append(new LLOAD(2));
		iList.append(invoke);
		iList.append(new LCMP());
		iList.append(new IFNE(ihTemp));//9->14
		iList.append(new ICONST(1));
		iList.append(new IRETURN());
		iList.append(new ICONST(0));
		iList.append(new IRETURN());
		
		iList.setPositions();
		
		ih = iList.findHandle(9);
		((IFNE)ih.getInstruction()).setTarget(iList.findHandle(14));
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.BOOLEAN
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "Equal", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
		//Not Equal
		
		iList = new InstructionList();
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(invoke);
		iList.append(new LLOAD(2));
		iList.append(invoke);
		iList.append(new LCMP());
		iList.append(new IFNE(ihTemp));//9->14
		iList.append(new ICONST(0));
		iList.append(new GOTO(ihTemp));//13->17
		iList.append(new ICONST(1));
		iList.append(new IRETURN());
		
		
		iList.setPositions();
		
		ih = iList.findHandle(9);
		((IFNE)ih.getInstruction()).setTarget(iList.findHandle(16));
		
		ih = iList.findHandle(13);
		((GOTO)ih.getInstruction()).setTarget(iList.findHandle(17));
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.BOOLEAN
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "NotEqual", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
		//Mul
		iList = new InstructionList();
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(invoke);
		iList.append(new LLOAD(2));
		iList.append(invoke);
		iList.append(new LMUL());
		iList.append(new LRETURN());
		
		iList.setPositions();
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.LONG
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "Mul", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
		//Div
		iList = new InstructionList();
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(invoke);
		iList.append(new LLOAD(2));
		iList.append(invoke);
		iList.append(new LDIV());
		iList.append(new LRETURN());
		
		iList.setPositions();
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.LONG
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "Div", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
		//Mod
		iList = new InstructionList();
		
		ihTemp = iList.append(new LLOAD(0));
		iList.append(invoke);
		iList.append(new LLOAD(2));
		iList.append(invoke);
		iList.append(new LREM());
		iList.append(new LRETURN());
		
		iList.setPositions();
		
		mg = new MethodGen(Constants.ACC_STATIC | Constants.ACC_PUBLIC, Type.LONG
				, new Type[] {Type.LONG, Type.LONG}, new String[] {"a", "b"}
				, "Mod", "Test", iList, cpg);
		
		mg.setMaxLocals();
		mg.setMaxStack();
		cg.addMethod(mg.getMethod());	
		
		iFac = new InstructionFactory(cg);
		
			
	}
	
	private static long ConvertValue(long l)
	{
		long temp = l;
		
		long ret = 0;
		
		for(int i = 0; i < 33; ++i)
		{
			if(temp < 1)break;
			
			if((temp % 2) == 1)
				ret += Math.pow(2, 32 - i );

			temp = temp / 2;
		}
		
		return ret;
	}
	
	private void InjectionMethods(ClassGen cg, MethodGen mg)
	{
		ConstantPoolGen cpg = cg.getConstantPool();
		InstructionList iList = new InstructionList();
		InstructionFactory iFac = new InstructionFactory(cg);
		
		MethodGen newMg = new MethodGen(mg.getMethod(), cg.getClassName(),
                cg.getConstantPool());
		
		//새로운 리스트에 현재 인스트럽션을 넣는다.
		
		InvokeInstruction invoke = iFac.createInvoke("Test", "ConvertValue", Type.LONG
				, new Type[] {Type.LONG}, Constants.INVOKESTATIC);
		
		InstructionHandle dummy = mg.getInstructionList().getInstructionHandles()[0];
		
		ArrayList<InstructionHandle> ihPosition = new ArrayList<InstructionHandle>();
		ArrayList<InstructionHandle> ihTarget = new ArrayList<InstructionHandle>();
		
		
		for(InstructionHandle ih: mg.getInstructionList().getInstructionHandles())
		{
			if(ih.getInstruction() instanceof BranchInstruction)
			{
				//Branch구문은 코드가 추가되면 바이트코드 위치가 다 바뀐다.
				//그러므로 미리 핸들을 저장해놓고 처리한다.
				
				ihTarget.add(((BranchInstruction)ih.getInstruction()).getTarget());
				((BranchInstruction)ih.getInstruction()).setTarget(dummy);
				ihPosition.add(ih);
				iList.append((BranchInstruction)ih.getInstruction());
			}
			else /*if(!(ih.getInstruction() instanceof ArithmeticInstruction))*/
				iList.append(ih.getInstruction());
			/*
			if(ih.getInstruction() instanceof LLOAD)
			{
				//LLOAD라는것은 변수를 불러온것.
				//처리할땐 무조건 함수를 거쳐서 처리한다.
				iList.append(invoke);
				
				//int iValue = ((LLOAD)ih.getInstruction()).getIndex();
				
				//iList.append(new LSTORE(iValue));
			}
			else */
				
			/*
			if(ih.getInstruction() instanceof ArithmeticInstruction)
			{
				System.out.println("Temp");
				invoke = iFac.createInvoke("Test", "Mul", Type.LONG
						, new Type[] {Type.LONG, Type.LONG}, Constants.INVOKESTATIC);
				iList.append(invoke);
			}
			*/
		}
		
		//한바퀴 더 돌면서 브랜치 위치 잡아줌.
		
		iList.setPositions();
		
		int iPos = 0;
		
		/*
		if(ihPosition.size() != 0)
		{
			for(InstructionHandle ih : iList.getInstructionHandles())
			{
				for(InstructionHandle ih2: iList.getInstructionHandles())
				{
					if((ihPosition.get(iPos).getInstruction() == ih.getInstruction()) && (ihTarget.get(iPos).getInstruction() == ih2.getInstruction()))
					{
						
						((BranchInstruction)ih.getInstruction()).setTarget(ih2);
						++iPos;
					}
				}
				
				if(iPos == ihPosition.size() - 1)
					break;
			}
		}		
		*/
		for(int i = 0; i < ihPosition.size(); ++i)
		{
			for(InstructionHandle ihh: iList.getInstructionHandles())
			{
				if(ihh.getInstruction() == ihTarget.get(i).getInstruction())
				{
					((BranchInstruction)ihPosition.get(i).getInstruction()).setTarget(ihh);
				}
			}
			
		}
		

		newMg.setInstructionList(iList);
		
        newMg.setMaxLocals();
        newMg.setMaxStack();
        
        cg.replaceMethod(mg.getMethod(), newMg.getMethod());
	}
}
