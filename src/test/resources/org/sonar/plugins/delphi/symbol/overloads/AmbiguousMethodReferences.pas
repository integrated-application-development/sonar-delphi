unit AmbiguousMethodReferences;

interface

type
  TFunctionType = function: String;

procedure Foo(Bar: TFunctionType); overload;
procedure Foo(Bar: String); overload;

implementation

function GetString: String;
begin
  Result := 'String';
end;

procedure Foo(Bar: TFunctionType);
begin
  // Do nothing
end;

procedure Foo(Bar: String);
begin
  // Do nothing
end;

procedure Test;
var
  FuncVar: TFunctionType;
  StringVar: String;
begin
  Foo(FuncVar);
  Foo(GetString);
  Foo(StringVar);
  Foo('String literal');
end;

end.