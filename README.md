# CMMInterpreter   
## JAVA版CMM解释器   
### 语法分析：  
文法  
> + program -> stmt-sequence  
> + stmt-sequence -> statement ; stmt-sequence | statement | ε  
> + statement -> if-stmt | while-stmt | assign-stmt | read-stmt | write-stmt | declare-stmt  
> + stmt-block -> statement | { stmt-sequence }  
> + if-stmt -> if ( cond ) then stmt-block | if ( exp ) then stmt-block else stmt-block  
> + while-stmt -> while ( cond ) stmt-block   
> + assign-stmt -> variable = exp ;  
> + read-stmt -> read variable ;  
> + write-stmt -> write exp ;  
> + declare-stmt -> (int | real) ( (identifier [= exp ]) | (identifier [ exp ] [={value-list}]) ) ;  
> + value-list -> exp more-value  
> + more-value -> , exp more-value|ε 
> + variable -> identifier [ [ exp ] ]  
> + exp-> cond  
> + cond -> exp more-exp | logical-op cond  
> + more-exp -> logical-op exp more-exp |ε  
> + gen-exp -> addtive-exp more-addt-exp  
> + more-add-exp -> compare-op addtive-exp more-addt-exp |ε  
> + addtive-exp -> term more-term  
> + more-term -> add-op additive-exp more-term|ε  
> + term -> factor more-factor  
> + more-factor -> mul-op term|ε  
> + factor -> ( exp ) | number | variable | Add-op term |NULL  
> + number -> int-val | real-val  
> + logical-op -> ! | || | &&  
> + compare-op -> > | < | >= | <= | <> | ==   
> + add-op -> + | -   
> + mul-op -> * | /   
