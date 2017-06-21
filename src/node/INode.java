package node;

import main.SemanticError;
import symbol_table.Environment;
import type.Type;
import type.TypeException;

import java.util.ArrayList;

public interface INode {

    ArrayList<SemanticError> checkSemantics(Environment env);

    Type type() throws TypeException;

    String codeGeneration();

    ArrayList<INode> getChilds();

}