unit Labels;

interface

implementation

procedure Identifier;
label Foo;
begin
  goto Foo;
  Foo:
end;

procedure Decimal;
label 12345;
begin
  goto 12345;
  12345:
end;

procedure Hex;
label $3039;
begin
  goto $3039;
  $3039:
end;

procedure Binary;
label %11000000111001;
begin
  goto %11000000111001;
  %11000000111001:
end;


end.