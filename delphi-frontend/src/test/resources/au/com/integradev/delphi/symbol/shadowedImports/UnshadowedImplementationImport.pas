unit UnshadowedImplementationImport;

interface

type
  ShadowedName = class(TObject)
  public
    class procedure Foo;
  end;

implementation

uses ShadowedName;

initialization
  ShadowedName.Foo; // Should reference other unit
end.