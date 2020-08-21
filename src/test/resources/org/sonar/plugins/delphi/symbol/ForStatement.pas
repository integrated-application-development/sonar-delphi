unit ForStatement;

interface

implementation

procedure Test(IntArray: TArray<Integer>);
var
  I: Integer;
begin
  for I in IntArray do begin
    // Do nothing
  end;

  for I := 0 to 100 do begin
    // Do nothing
  end;

  for I := 100 downto 0 do begin
    // Do nothing
  end;
end;

end.