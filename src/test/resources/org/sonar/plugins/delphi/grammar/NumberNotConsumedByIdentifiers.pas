unit NumberNotConsumedByIdentifiers;

interface

implementation

procedure NumberNotConsumedByIdentifiersTest;
begin
  for Index := 0 to 10do begin // Previously, this would improperly tokenize as 'for' '0' 'to' '10d' 'o' 'begin'
    DoStuff;         // Where 10d is a sequence of hexadecimal digits Hexdigitseq.
  end;
end;

end.

