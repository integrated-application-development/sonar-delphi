unit Unit2;

interface

uses Unit1;
type
  class2 = class
  public
    class1_: class1;
    function function2: String;
    function function3: String;
    property SomeProperty: String read function1 write function2;
  end;

implementation

function class2.function2: String;
begin
  Result := class1_.function1;
end;

end.