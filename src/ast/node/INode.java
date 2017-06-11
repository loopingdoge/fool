package ast.node;

import ast.type.Type;
import ast.type.TypeException;
import util.Environment;
import util.SemanticError;

import java.util.ArrayList;

public interface INode {

    ArrayList<SemanticError> checkSemantics(Environment env);

    Type type() throws TypeException;

    String codeGeneration();

    ArrayList<INode> getChilds();

}