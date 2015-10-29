import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Created by Raghu on 10/24/2015.
 */
public class ConstructUMLInput {

    public static String GetClasName(ClassOrInterfaceDeclaration n){
        return n.getName();
    }

    public static List getImplementsList(ClassOrInterfaceDeclaration clss) {
            return clss.getImplements();
    }

    public static List getExtendsList(ClassOrInterfaceDeclaration clss) {
        return clss.getExtends();
    }

//    public  static void GetAssociation(MethodDeclaration n){
//        String st;
//        List<Statement> stmts = n.getBody().getStmts();
//        for(Statement li : stmts){
//            st = li.toString();
//            if(st.contains("new ")){
//                com.sun.deploy.util.StringUtils
//            }
//        }
//
//
//    }
}
