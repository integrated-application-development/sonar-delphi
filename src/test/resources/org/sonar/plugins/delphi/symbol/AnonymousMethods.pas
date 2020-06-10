unit AnonymousMethods;

{This is a sample Delphi file.}

interface

type
  TAnonymousProcedure = reference to procedure(Index: Integer);
  TAnonymousFunction = reference to function: Integer;

implementation

procedure Foo(Proc: TAnonymousProcedure); overload;
begin
  Proc;
end;

procedure Foo(Func: TAnonymousFunction); overload;
begin
  Func;
end;

procedure Foo(Int: Integer); overload;
begin
  // Do nothing
end;

procedure Bar(Proc: TAnonymousProcedure; Func: TAnonymousFunction);
var
  BarIndex: Integer;
begin
  Foo(Proc);
  Foo(procedure(Index: Integer) 
  begin
    Index := BarIndex;
  end);
  
  Foo(Func);
  Foo(function: Integer
  begin
    Foo(Result);
  end);
end;

end.