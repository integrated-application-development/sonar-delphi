Unit IfNotTest;

interface

var
   condition : Boolean;
   text: String;

implementation

 begin
   condition := false;
   if not (condition = false) // non-compliant
   then text := '17 squared > 400'    // Action when if condition is true
   else text := '17 squared <= 400';  // Action when if condition is false
 end.