unit ShadowedImplementationImport;

interface

implementation

uses ShadowedName;

type
  ShadowedName = class(TObject)
  public
    class procedure Foo;
  end;

initialization
  ShadowedName.Foo; // Should reference class procedure
end.