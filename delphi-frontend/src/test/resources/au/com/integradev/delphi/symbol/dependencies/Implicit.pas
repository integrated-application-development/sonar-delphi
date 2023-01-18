unit Implicit;

interface

implementation

uses
  System.SysUtils;

procedure Test;
var
  Obj: TObject;
begin
  Obj := TObject.Create;
  FreeAndNil(Obj);
end;

end.