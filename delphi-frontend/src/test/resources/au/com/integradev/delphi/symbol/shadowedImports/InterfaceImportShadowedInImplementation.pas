unit InterfaceImportShadowedInImplementation;

interface

uses ShadowedName;

procedure Foo(Id: Integer = ShadowedName.Bar);

implementation

type
  ShadowedName = class
  public const
    Bar = 5;
  end;

end.