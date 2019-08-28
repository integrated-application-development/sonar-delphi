unit Simple;

{This is a sample Delphi file.}

interface

type
  TByteSet = set of Byte;

procedure Foo(Bar: Integer); overload;
procedure Foo(Bar: String); overload;
procedure Foo(Bar: TByteSet);

implementation

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

procedure Foo(Bar: String);
begin
  // Do nothing
end;

procedure Foo(Bar: TByteSet);
begin
  // Do nothing
end;

procedure Test;
var
  Arg1: Integer;
  Arg2: String;
  Arg3: set of Byte;
begin
  Foo(Arg1);
  Foo(Arg2);
  Foo(Arg3);
  Foo([0,1,2,3,4]);
end;

end.