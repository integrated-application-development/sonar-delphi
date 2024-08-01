unit Test;
{$I QuickFixesFollowingIncludeDirectives.inc}
interface
type
  TFoo = class(TObject)
    procedure Foo;
    procedure Bar;
  end;

implementation

procedure TFoo.Foo;
begin
  // Fix qf1@[+2:2 to +3:2] <<>>
  // Noncompliant@+1
  inherited;
  B := 1;
end;

procedure TFoo.Bar;
begin
  var B := 1;
  inherited;
  // Fix qf2@[-2:13 to -1:12] <<>>
  // Noncompliant@-2
end;

end.