import com.github.javaparser.ast.CompilationUnit;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import  com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.SourceStringReader;
import javax.print.DocFlavor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by Raghu on 10/25/2015.
 */
public class UMLParser {

    //Global Variables

    public static String strSource;
    public static String strRelations = "";
    public static String strUses = "";
    public static String strCls = "";

    public  static  boolean bCallFirst = true;

    public static List<String> lAllClassList = new CopyOnWriteArrayList<String>();
    public static List<String> lClassList = new CopyOnWriteArrayList<String>();
    public static List<String> lInterfaceList = new CopyOnWriteArrayList<String>();
    public static List<String> lInheritanceList = new CopyOnWriteArrayList<String>();
    public static List<String> lUsesList = new CopyOnWriteArrayList<String>();
    public static List<String> lAssocList = new CopyOnWriteArrayList<String>();
    public static List<String> lAllAssocList = new CopyOnWriteArrayList<String>();
    public static List<String> lMethodlist = new CopyOnWriteArrayList<String>();

    public static void main(String[] args) throws Exception {

        String dest;
        File folder;
        File[] listOfFiles;
        String filename, cname;

        if (args.length < 2) {
            System.out.println("Invalid number of arguments, try again");
            System.out.println("Ex: umlparser <source dir> <output file path>");
            return;
        }

        dest = args[1];
        folder = new File(args[0].trim());
        listOfFiles  = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File myFileScanned = listOfFiles[i];
            if (myFileScanned.isFile() && myFileScanned.getName().endsWith(".java")) {
                FileInputStream in = new FileInputStream(myFileScanned.getPath());
                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                } finally {
                    in.close();
                }
                new GetImplementsAndExtends2().visit(cu, null);
            }
        }

        // write @startUml for plantUML
        strSource = "@startuml\n";
        strSource = strSource + "skinparam classAttributeIconSize 0\n";
        System.out.println("@startuml");

        //loop through each .java files and construct input for plant UML

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile() && file.getName().endsWith(".java")) {
                System.out.println("---------*****  " + file.getName() + "  *****----------- ");
                FileInputStream in = new FileInputStream(file.getPath());
                CompilationUnit cu;
                try {
                    // parse the file
                    cu = JavaParser.parse(in);
                } finally {
                    in.close();
                }

                new GetImplementsAndExtends().visit(cu, null);

               // bCallFirst = true;
                new MethodVisitor1().visit(cu, null);

                new MemberVisitor().visit(cu,null);

                new ConstructorVisitor().visit(cu, null);

              //  bCallFirst = false;
                new MethodVisitor().visit(cu, null);

                new GetClassDetails().getUsesList();

                strSource = strSource + "\n}\n";

                if (strRelations != null && !strRelations.isEmpty()) {
                    strSource = strSource + strRelations;
                    System.out.println(strRelations);
                }
                if (strUses != null && !strUses.isEmpty()) {
                    strSource = strSource + strUses;
                    System.out.println(strUses);
                }
                strRelations = "";
                strUses = "";
                lUsesList.clear();
                lAssocList.clear();
            }
        }


        System.out.println(strSource);
        System.out.println("Hi");

        strSource = strSource + "\n@enduml";
        System.out.println("@enduml");

        System.out.println("Im the source - "+ strSource);

        OutputStream png = new FileOutputStream(dest);
        SourceStringReader myreader = new SourceStringReader(strSource);
        // Write the UML diagram to png
        String desc = myreader.generateImage(png);
        // Return a null string if no generation
        System.out.println(desc);

    }

    private static class GetImplementsAndExtends2 extends VoidVisitorAdapter {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {

            String strCls = ConstructUMLInput.GetClasName(n);

            if (n.isInterface()) {
                lInterfaceList.add(strCls);
                lAllClassList.add(strCls);
            } else {
                lClassList.add(strCls);
                lAllClassList.add(strCls);
            }
        }
    }

    private static class GetImplementsAndExtends extends VoidVisitorAdapter {

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {


            strCls = ConstructUMLInput.GetClasName(n);
            List<ClassOrInterfaceDeclaration> listimplements = new ArrayList();
            List<ClassOrInterfaceDeclaration> listextends = new ArrayList();

            if (n.isInterface()) {
                strSource += "interface " + strCls + " {\n";
            } else {
                strSource += "class " + strCls + " {\n";
            }

            listextends =  ConstructUMLInput.getExtendsList(n);
            String name;
            Iterator iterator1 = listextends.iterator();
            if (iterator1.hasNext()) {
                while (iterator1.hasNext()) {
                    name = iterator1.next().toString();
                    strRelations += name + " <|-- " + strCls + ": inherits" + "\n";
                    lInheritanceList.add(name);
                }
            }

            listimplements = ConstructUMLInput.getImplementsList(n);
            Iterator iterator2 = listimplements.iterator();

            if (iterator2.hasNext()) {
                while (iterator2.hasNext()) {
                    name = iterator2.next().toString();
                    strRelations += name + " <|.. " + strCls + ": interface" + "\n";
//                    if(!lInterfaceList.contains(name)){
//                        lInterfaceList.add(name);
//                    }
                }
            }

            System.out.println("Parent class  " + n.getExtends());
            System.out.println("interfaces are - " + n.getImplements());
        }
    }

    private static class ConstructorVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(ConstructorDeclaration n, Object arg) {
            String constructor;
            String consName = n.getName();
            List constParam = n.getParameters();
            List ParamType = n.getTypeParameters();
            int accessModifier = n.getModifiers();
            String modifier = "- ";
            if (accessModifier == 1) {
                modifier = "+ ";
            }else if(accessModifier == 0){
                modifier = "- ";
            }

            constructor = modifier + consName + "(";
            for (int i = 0; i < constParam.size(); i++) {
                constructor = constructor + constParam.get(i).toString();
                // constructor = constructor + " : " + ParamType.get(i);
                if (i < constParam.size() - 1) {
                    constructor += ", ";
                }
            }
            constructor += ")\n";
            strSource += constructor;

            String strTypes;
            for (int i = 0; i < constParam.size(); i++) {
                strTypes = constParam.get(i).toString();
                strTypes = strTypes.substring(0, strTypes.indexOf(" "));
                for (String temp : lAllClassList) {
                    if(strTypes.equals(temp)){
                        if(!lUsesList.isEmpty()){
                            if(lUsesList.contains(strTypes)){
                                strTypes = "";
                            }
//                            for (String name : lUsesList) {
//                                if(strTypes.equals(name)){
//                                    strTypes = "";
//                                }
//                            }
                        }

                        if(!strTypes.isEmpty()){
                            lUsesList.add(strTypes);
                        }

                    }
                }
            }
        }
    }

    private static class MethodVisitor1 extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {

            String strType;
            String strMethd = n.getName().toString();
            String strRet = n.getType().toString();
            List<Parameter> lParam = n.getParameters();

            int iMod = n.getModifiers();


                if(iMod == 1) {
                    lMethodlist.add(strMethd.toUpperCase());
                }

        }
    }

    private static class MemberVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(FieldDeclaration n, Object args) {
            String strType = n.getType().toString();
            List mylist = n.getVariables();
            int Imod = n.getModifiers();
            boolean bl=false;
            String var="";

            if(strType.contains("Collection")){
                strType = strType.substring(strType.indexOf("<") + 1, strType.indexOf(">"));
                var = (mylist.get(0).toString().split(" ")[0]);
                bl=true;
            }


            for (int i = 0; i < mylist.size(); i++) {
                if ( Imod == 1 ){

                    if(!bl)
                        strSource += "+ " + mylist.get(i)  + ":" + strType + "\n";
                    else
                        strSource += "+ " + var  + ":" + strType + "\n";

//                    for (String temp : lClassList) {
//
//                        if (strType.equals(temp)) {
//
//                            if (!lUsesList.isEmpty()) {
//                                for (String name : lUsesList) {
//                                    if (strType.equals(name)) {
//                                        strType = "";
//                                    }
//                                }
//                            }
//
//                            if (!strType.isEmpty()) {
//                                lUsesList.add(strType);
//                                System.out.println(lUsesList);
//                            }
//                        }
//                    }
                }
                else if(Imod == 2){
                    String strSetmtd;
                    String strGetmtd;

                    strSetmtd = ("set"+mylist.get(i)).toUpperCase();
                    strGetmtd = ("get"+mylist.get(i)).toUpperCase();

                    if(lMethodlist.contains(strGetmtd) && lMethodlist.contains(strSetmtd)){
                        lMethodlist.remove(strGetmtd);
                        lMethodlist.remove(strSetmtd);

                        strSource += "+ " + mylist.get(i)  + ":" + strType + "\n";

                    }
                    else
                    {
                        if(!bl)
                            strSource += "- " + mylist.get(i)  + ":" + strType + "\n";
                        else
                            strSource += "- " + var  + ":" + strType + "\n";


                    }
//                    for (String temp : lClassList) {
//
//                        if (strType.equals(temp)) {
//
//                            if (!lUsesList.isEmpty()) {
//                                for (String name : lUsesList) {
//                                    if (strType.equals(name)) {
//                                        strType = "";
//                                    }
//                                }
//                            }
//
//                            if (!strType.isEmpty()) {
//                                lUsesList.add(strType);
//                                System.out.println(lUsesList);
//                            }
//                        }
//                    }
                }


                for (String cls : lAllClassList) {



                    if (strType.equals(cls)) {
                        if (!lAssocList.isEmpty()) {
                            for (String exist : lAssocList) {
                                if (strType.equals(exist)) {
                                    strType = "";

                                }
                            }
                        }

                        if (!strType.isEmpty()){

                            if (!lAllAssocList.isEmpty()) {
                                if(!lAllAssocList.contains(strType+":"+strCls)){
                                    lAssocList.add(strType);
                                    lAllAssocList.add(strCls+":"+strType);
                                }
                            }else{
                                lAssocList.add(strType);
                                lAllAssocList.add(strCls+":"+strType);
                            }

                        }
                    }
                }


            }
        }
    }

    public static class GetClassDetails {

        public static void getUsesList() {

            if (!lUsesList.isEmpty()) {
                for (String uses : lUsesList) {
                    if (!lInheritanceList.isEmpty()) {
                        for (String extnds : lInheritanceList) {
                            if (uses.equals(extnds)) {
                                lUsesList.remove(uses);
                            }
                        }
                    }
                }
            }


            if(!lUsesList.isEmpty()&& !lUsesList.contains(strCls)){
                for (String temp : lUsesList) {
                    if(!(temp.equals(strCls))){
                        strUses  += temp + "<.. " + strCls + ": uses" + "\n";
                    }
                }
            }


            if(!lAssocList.isEmpty() && !lAssocList.contains(strCls)){
                for (String ass : lAssocList) {
                    if(!(ass.equals(strCls))){
                        strRelations  += ass + "-- " + strCls +  "\n";
                    }
                }
            }

            lUsesList.clear();
        }

    }

    private static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {

            String strType;
            String strMethd = n.getName().toString();
            String strRet = n.getType().toString();
            List<Parameter> lParam = n.getParameters();

            int iMod = n.getModifiers();

            if(iMod == 1 && lMethodlist.contains(strMethd.toUpperCase())){

                strSource += "+ " + strMethd + "(";
                for (int i = 0; i < lParam.size(); i++) {
                    strSource += lParam.get(i).toString();
                    strType = lParam.get(i).toString();
                    strType = strType.substring(0, strType.indexOf(" "));

                    if(lAllClassList.contains(strType)){
                        if (!lUsesList.isEmpty()) {
                            for (String name : lUsesList) {
                                if (strType.equals(name)) {
                                    strType = "";
                                }
                            }
                        }

                        if (!strType.isEmpty()) {
                            lUsesList.add(strType);
                        }

                    }

                    if (i < lParam.size() - 1) {
                        strSource += ", ";
                    }
                }
                strSource += ")" + strRet + " \n";
            }
            else if(iMod ==0 ){

                for (int i = 0; i < lParam.size(); i++) {

                    strType = lParam.get(i).toString();
                    strType = strType.substring(0, strType.indexOf(" "));

                    for (String temp : lAllClassList) {
                        if (strType.equals(temp)) {

                            if (!lUsesList.isEmpty()) {
                                for (String name : lUsesList) {
                                    if (strType.equals(name)) {
                                        strType = "";
                                    }
                                }
                            }

                            if (!strType.isEmpty()) {
                                lUsesList.add(strType);
                            }
                        }
                    }
                }
            }
        }
    }

}
