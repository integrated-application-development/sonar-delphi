unit AnonymousMethods;

{This is a sample Delphi file.}

interface

type
  TAnonymousProcedure = reference to procedure(Index: Integer);

implementation

procedure Foo(Proc: TAnonymousProcedure);
begin
  Proc;
end;

procedure Bar(Proc: TAnonymousProcedure);
var
  BarIndex: Integer;
begin
  Foo(Proc);
  Foo(procedure(Index: Integer) 
  begin
    Index := BarIndex;
  end);
end;

end.