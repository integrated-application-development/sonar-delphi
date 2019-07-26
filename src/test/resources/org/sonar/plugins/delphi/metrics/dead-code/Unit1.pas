unit Unit1;

{This is a sample Delphi file.}

interface

uses Unit2;

type
  class1 = class
    function function1: String;
  end;
implementation

function class1.function1: String;
begin
  Result := class2_.function2;
end;

end.
