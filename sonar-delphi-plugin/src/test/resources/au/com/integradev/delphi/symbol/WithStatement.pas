unit WithStatement;

interface

type
  TFoo = record
    A: String;
    B: String;
  end;

  TBar = record
    A: String;
  end;

implementation

procedure Test(Foo: TFoo; Bar: TBar);
var
  A: String;
  B: String;
  C: String;
begin
  with Foo, Bar do begin
    A := 'Resolves to member TBar.A';
    B := 'Resolves to member TFoo.B';
    C := 'Resolves to local variable C';
  end;  
end;

function GetFoo: TFoo;
var
  Foo: TFoo;
begin
  Result := Foo;
end;

procedure Test;
var
  A: String;
begin
  with GetFoo do begin
    A := 'Resolves to member TFoo.A';
  end;  
end;

end.