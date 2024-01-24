unit InterfaceImportShadowedLater;

interface

uses ShadowedName;

procedure Foo(Id: Integer = ShadowedName.Bar);

type
  ShadowedName = class
  public const
    Bar = 5;
  end;

implementation

end.