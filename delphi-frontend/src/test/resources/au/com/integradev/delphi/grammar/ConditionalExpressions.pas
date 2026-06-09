unit ConditionalExpressions;

interface

implementation

function Describe(aValue : Integer) : string;
begin
  Result := if aValue > 0 then 'positive' else 'non-positive';
end;

function PickInt(aFlag : Boolean; aLhs, aRhs : Integer) : Integer;
begin
  Result := if aFlag then aLhs else aRhs;
end;

procedure UseInArgument;
begin
  Describe(if True then 1 else 2);
end;

function Nested(aA, aB : Boolean) : Integer;
begin
  Result :=
    if aA then
      if aB then 1 else 2
    else
      if aB then 3 else 4;
end;

function Mixed(aFlag : Boolean) : Integer;
begin
  Result := (if aFlag then 10 else 20) + 1;
end;

end.
