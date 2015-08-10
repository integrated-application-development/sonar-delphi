unit UnusedARgumentRule;

interface

uses Classes;

type
  TCustomComponent = class(TComponent)
  protected
    procedure OnEvent(Sender: TObject);
    procedure OnEventB(ASender: TObject);
    procedure OnUnusedArg(x: Integer);
  public
  end;

implementation


{ TCustomComponent }

procedure TCustomComponent.OnEvent(Sender: TObject);
begin
  Writeln('dummy');
end;

procedure TCustomComponent.OnEventB(ASender: TObject);
begin
  Writeln('dummy');
end;

procedure TCustomComponent.OnUnusedArg(x: Integer);
begin
  Writeln('dummy');
end;

end.