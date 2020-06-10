unit BareInterfaceMethodReference;

{This is a sample Delphi file.}

interface

function Foo(Baz: Integer): Integer;
function ExternalFunc(Result: Booelan): Boolean;

implementation

procedure Bar(Flarp: Integer);
begin
  // Do nothing
end;

function Foo;
begin
  Bar(Baz);
end;

function ExternalFunc; external 'mydll.dll' name 'ExternalFunc';

end.