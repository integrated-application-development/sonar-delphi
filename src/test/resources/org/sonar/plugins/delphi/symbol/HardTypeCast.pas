unit HardTypeCast;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  private type
    TFooOption = (Option1, Option2, Option3);
  public
    function Foo(Bar: Integer): Boolean;
  end;

implementation

function TFoo.Foo(Bar: Integer): Boolean;
begin
  Result := TFooOption.Option1 = TFooOption(Bar);
end;

end.