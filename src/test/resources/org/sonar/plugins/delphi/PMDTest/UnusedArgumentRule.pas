unit UnusedARgumentRule;

interface

type
  TCustomComponent = class(TComponent)
  protected
    procedure OnEvent(Sender: TObject);
    procedure OnEventB(ASender: TObject);
    procedure OnUnusedArg(x: Integer);
  public
    procedure CaseInsensitive(Arg: Integer);
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

procedure TCustomComponent.CaseInsensitive(Arg: Integer);
begin
  arg := arg + 1;
end;

end.