unit Explicit;

interface

implementation

uses
  System.SysUtils;

procedure Test;
var
  Obj: TObject;
begin
  Obj := TObject.Create;
  System.SysUtils.FreeAndNil(Obj);
end;

end.