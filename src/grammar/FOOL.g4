grammar FOOL;

@header {
    import java.util.ArrayList;
}

@lexer::members {
   public ArrayList<String> errors = new ArrayList<>();
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

prog
    : exp SEMIC                 		    #singleExp
    | let exp SEMIC                 	    #letInExp
    | (classdec)+ SEMIC (let)? exp SEMIC	#classExp
    ;

classdec
    : CLASS ID ( IMPLEMENTS ID )? (LPAR (vardec ( COMMA vardec)*)? RPAR)?  (CLPAR ((met SEMIC)+)? CRPAR)?
    ;

let
    : LET (dec SEMIC)+ IN
    ;

vardec
    : type ID
    ;

varasm
    : vardec ASM exp
    ;

fun
    : type ID LPAR ( vardec ( COMMA vardec)* )? RPAR (let)? exp
    ;

met
    : fun
    ;

dec
    : varasm           #varAssignment
    | fun              #funDeclaration
    ;


type
    : INT
    | BOOL
    | ID
    ;

exp
    :  ('-')? left=term (operator=(PLUS | MINUS) right=exp)?
    ;

term
    : left=factor (operator=(TIMES | DIV) right=term)?
    ;

factor
    : left=value (operator=(AND | OR | GEQ | EQ | LEQ | GREATER | LESS) right=value)?
    ;

value
    : INTEGER                        		                                        #intVal
    | (NOT)? ( TRUE | FALSE )                  		                                #boolVal
    | LPAR exp RPAR                      			                                #baseExp
    | IF cond=exp THEN CLPAR thenBranch=exp CRPAR ELSE CLPAR elseBranch=exp CRPAR   #ifExp
    | ID                                                                            #varExp
    | THIS                                                                          #thisExp
    | funcall                                                                       #funExp
    | (ID | THIS) DOT funcall                             	                        #methodExp
    | NEW ID (LPAR ( exp (COMMA exp)* )? RPAR)?			                            #newExp
    | PRINT ( exp )                                                                 #print
    ;

funcall
    : ID ( LPAR (exp (COMMA exp)* )? RPAR )
    ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/
SEMIC  : ';' ;
COLON  : ':' ;
COMMA  : ',' ;
EQ     : '==' ;
LEQ    : ('<=' | '=<') ;
GEQ    : ('>=' | '=>') ;
GREATER: '>' ;
LESS   : '<' ;
AND    : '&&' ;
OR     : '||' ;
NOT    : '!' ;
ASM    : '=' ;
PLUS   : '+' ;
MINUS  : '-' ;
TIMES  : '*' ;
DIV    : '/' ;
TRUE   : 'true' ;
FALSE  : 'false' ;
LPAR   : '(' ;
RPAR   : ')' ;
CLPAR  : '{' ;
CRPAR  : '}' ;
IF     : 'if' ;
THEN   : 'then' ;
ELSE   : 'else' ;
LET    : 'let' ;
IN     : 'in' ;
VAR    : 'var' ;
PRINT  : 'print';
INT    : 'int' ;
BOOL   : 'bool' ;
CLASS  : 'class' ;
IMPLEMENTS   : 'implements' ;
THIS   : 'this' ;
NEW    : 'new' ;
DOT    : '.' ;


//Numbers
fragment DIGIT  : '0'..'9';
INTEGER         : DIGIT+;

//IDs
fragment CHAR   : 'a'..'z' |'A'..'Z' ;
ID              : CHAR (CHAR | DIGIT)* ;

//ESCAPED SEQUENCES
WS              : (' '|'\t'|'\n'|'\r')-> skip ;
LINECOMENTS     : '//' (~('\n'|'\r'))* -> skip ;
BLOCKCOMENTS    : '/*'( ~('/'|'*')|'/'~'*'|'*'~'/'|BLOCKCOMENTS)* '*/' -> skip ;

ERR     : . { errors.add("Invalid char: " + getText());} -> channel(HIDDEN) ;
