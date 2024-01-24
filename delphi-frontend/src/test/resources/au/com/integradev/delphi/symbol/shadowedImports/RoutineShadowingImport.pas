unit VarShadowingImport;

interface

uses ShadowedName;

type
  TFooObject = class(TObject)
  public
    procedure Foo;
  end;

function ShadowedName: TFooObject;

implementation

function ShadowedName: TFooObject;
begin
  // xyz
end;

initialization
  ShadowedName.Foo; // Should reference TFooObject.Foo
end.