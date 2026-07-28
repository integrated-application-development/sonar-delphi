unit IfExpressions;

interface

implementation

function Simple(Flag: Boolean): Integer;
begin
  Result := if Flag then 1 else 2;
end;

end.
