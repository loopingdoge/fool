grammar FOOL;

@lexer::members {
   //there is a much better way to do this, check the ANTLR guide
   //I will leave it like this for now just becasue it is quick
   //but it doesn't work well
   public int lexicalErrors=0;
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
    : CLASS ID ( IMPLEMENTS ID )? (LPAR vardec ( COMMA vardec)* RPAR)?  (CLPAR (fun SEMIC)+ CRPAR)?
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
    : left=value (EQ right=value)?
    ;

value
    : INTEGER                        		                                        #intVal
    | ( TRUE | FALSE )                  		                                    #boolVal
    | LPAR exp RPAR                      			                                #baseExp
    | IF cond=exp THEN CLPAR thenBranch=exp CRPAR ELSE CLPAR elseBranch=exp CRPAR   #ifExp
    | ID                                                                            #varExp
    | THIS                                                                          #thisExp
    | ID ( LPAR (exp (COMMA exp)* )? RPAR )                                         #funExp
    | (ID | THIS) DOT ID ( LPAR (exp (COMMA exp)* )? RPAR )	                        #methodExp
    | NEW ID (LPAR exp (COMMA exp)* RPAR)?			                                #newExp
    ;


/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/
SEMIC  : ';' ;
COLON  : ':' ;
COMMA  : ',' ;
EQ     : '==' ;
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
//PRINT : 'print' ;
LET    : 'let' ;
IN     : 'in' ;
VAR    : 'var' ;
FUN    : 'fun' ;
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

//VERY SIMPLISTIC ERROR CHECK FOR THE LEXING PROCESS, THE OUTPUT GOES DIRECTLY TO THE TERMINAL
//THIS IS WRONG!!!!
ERR     : . { System.out.println("Invalid char: "+ getText()); lexicalErrors++; } -> channel(HIDDEN) ;
