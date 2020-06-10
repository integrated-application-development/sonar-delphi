unit UnitWithDefaultArrayPropertyBackedByInlineMethod;

interface

type
  TFoo = class(TObject)
    function ReadMethod(Index: Integer): TObject; inline;
    procedure WriteMethod(Index: Integer; Obj: TObject); inline;
    property InlineMethodProperty[Index: Integer]: TObject read ReadMethod write WriteMethod; default;
  end;

implementation

uses
  System.SysUtils;

function TFoo.ReadMethod(Index: Integer): TObject;
begin
  Result := nil;
end;

procedure TFoo.WriteMethod(Index: Integer; Obj: TObject);
begin
  FreeAndNil(Obj);
end;

end.