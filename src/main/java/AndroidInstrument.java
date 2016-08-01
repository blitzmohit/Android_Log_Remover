import soot.*;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.options.Options;

import java.util.Iterator;
import java.util.Map;


public class AndroidInstrument {

    public static void main(String[] args) {

        //prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);

        //output as APK, too//-f J
        Options.v().set_output_format(Options.output_format_dex);

        // resolve the PrintStream and System soot-classes
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                final PatchingChain<Unit> units = b.getUnits();

                //important to use snapshotIterator here
                for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
                    final Unit u = iter.next();
                    u.apply(new AbstractStmtSwitch() {

                        public void caseInvokeStmt(InvokeStmt stmt) {
                            InvokeExpr invokeExpr = stmt.getInvokeExpr();
                            if(invokeExpr.getMethod().getSignature().equals("<android.util.Log: int v(java.lang.String,java.lang.String)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int e(java.lang.String,java.lang.String)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int i(java.lang.String,java.lang.String)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int w(java.lang.String,java.lang.String)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int wtf(java.lang.String,java.lang.String)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int v(java.lang.String,java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int e(java.lang.String,java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int i(java.lang.String,java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int w(java.lang.String,java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int wtf(java.lang.String,java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int w(java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int wtf(java.lang.String,java.lang.Throwable)>")
                                    || invokeExpr.getMethod().getSignature().equals("<android.util.Log: int println(int priority,java.lang.String,java.lang.String)")
                                    ){

                                G.v().out.println("detected and removing "+ invokeExpr.getMethod().getSignature());
                                Local tmpRef = addTmpRef(b);
                                Local tmpString = addTmpString(b);
                                units.remove(u);
                                //check that we did not mess up the Jimple
                                b.validate();
                            }
                        }

                    });
                }
            }


        }));

        soot.Main.main(args);
    }

    private static Local addTmpRef(Body body)
    {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String"));
        body.getLocals().add(tmpString);
        return tmpString;
    }
}