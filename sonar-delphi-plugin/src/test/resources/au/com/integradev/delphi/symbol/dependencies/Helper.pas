unit Helper;

interface

implementation

uses
  System.SysUtils;

function Foo: Boolean;
begin
  Result := HelperDependency.Bar.NONEXISTENT;
end;

function Bar: Boolean;
begin
  Result := ''.IsEmpty;
end;

end.