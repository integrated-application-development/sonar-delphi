unit NestedExpressions;

interface

type
  TByteSet = set of Byte;
  IntegerProvider = class
    function GetInteger(Param: TByteSet): Integer;
  end;

procedure Foo(Bar: Integer); overload;
procedure Foo(Bar: String); overload;

implementation

function GetString(Param: Integer): String;
begin
  Result := 'String';
end;

function IntegerProvider.GetInteger(Param: TByteSet): Integer;
begin
  Result := 12345;
end;

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

procedure Foo(Bar: String);
begin
  // Do nothing
end;

procedure Test;
var
  Provider: IntegerProvider;
  ByteSet: set of Byte;
begin
  Provider := IntegerProvider.Create;
  Foo(Provider.GetInteger(ByteSet));
  Foo(GetString(12345));
  Foo((((GetString(12345)))));
end;

end.