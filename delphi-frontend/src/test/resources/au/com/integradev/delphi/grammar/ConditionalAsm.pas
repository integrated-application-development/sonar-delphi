unit ConditionalAsm;

interface

implementation

procedure Foo;
{$if False}
asm
{$else}
begin
  var x := @Foo;
{$endif}
end;

end.