unit OptionalFunctionReturnType;

interface

function FunctionWithTypeDeclared: String;
function FunctionWithoutTypeDeclared: String;

implementation

function FunctionWithTypeDeclared: String;
begin
  Result := 'Result';
end;

function FunctionWithoutTypeDeclared;
begin
  Result := 'Result';
end;

end.
